package com.unitconverter.app.presentation.sheet;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.unitconverter.app.R;
import com.unitconverter.app.data.provider.UnitProvider;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.settings.AppSettings;
import com.unitconverter.app.settings.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Searchable unit list grouped by system (Metric, Imperial and US,
 * Indian traditional, Standard). Replaces the old dropdowns, which were slow
 * to scan once the Indian units were added.
 *
 * Result: fragment result {@link #REQUEST_KEY} with {@link #KEY_TARGET}
 * ("from" or "to") and {@link #KEY_UNIT_NAME}.
 */
public class UnitPickerSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "unit_pick";
    public static final String KEY_TARGET = "target";
    public static final String KEY_UNIT_NAME = "unit_name";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_SELECTED = "selected";

    private static final Unit.MeasureSystem[] GROUP_ORDER = {
            Unit.MeasureSystem.METRIC, Unit.MeasureSystem.IMPERIAL,
            Unit.MeasureSystem.INDIAN, Unit.MeasureSystem.OTHER
    };

    public static UnitPickerSheet newInstance(UnitCategory category, String target, String selectedName) {
        UnitPickerSheet sheet = new UnitPickerSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category.name());
        args.putString(KEY_TARGET, target);
        args.putString(ARG_SELECTED, selectedName);
        sheet.setArguments(args);
        return sheet;
    }

    private UnitCategory category;
    private String target;
    private String selected;
    private final List<Object> rows = new ArrayList<>();
    private RecyclerView list;
    private TextView empty;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        BottomSheetBehavior<?> behavior = dialog.getBehavior();
        behavior.setSkipCollapsed(true);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_unit_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        category = UnitCategory.valueOf(args.getString(ARG_CATEGORY));
        target = args.getString(KEY_TARGET, "to");
        selected = args.getString(ARG_SELECTED, "");

        ((TextView) view.findViewById(R.id.pickerTitle)).setText(
                "from".equals(target) ? R.string.choose_from_unit : R.string.choose_to_unit);
        list = view.findViewById(R.id.pickerList);
        empty = view.findViewById(R.id.pickerEmpty);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(new Adapter());

        EditText search = view.findViewById(R.id.pickerSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        filter("");
    }

    private void filter(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        boolean showIndian = AppSettings.of(requireContext()).showIndianUnits();
        List<Unit> units = UnitProvider.getInstance().getUnitsForCategory(category);
        rows.clear();
        for (Unit.MeasureSystem system : GROUP_ORDER) {
            if (system == Unit.MeasureSystem.INDIAN && !showIndian) continue;
            boolean headerAdded = false;
            for (Unit u : units) {
                if (u.getSystem() != system) continue;
                if (!q.isEmpty() && !matches(u, q)) continue;
                if (!headerAdded) {
                    rows.add(system);
                    headerAdded = true;
                }
                rows.add(u);
            }
        }
        empty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        if (list.getAdapter() != null) list.getAdapter().notifyDataSetChanged();
    }

    private static boolean matches(Unit u, String q) {
        return u.getName().toLowerCase(Locale.ROOT).contains(q)
                || u.getSymbol().toLowerCase(Locale.ROOT).contains(q)
                || u.getNote().toLowerCase(Locale.ROOT).contains(q);
    }

    static String systemLabel(Context c, Unit.MeasureSystem s) {
        switch (s) {
            case IMPERIAL: return c.getString(R.string.system_imperial);
            case INDIAN: return c.getString(R.string.system_indian);
            case OTHER: return c.getString(R.string.system_other);
            default: return c.getString(R.string.system_metric);
        }
    }

    private void pick(Unit unit) {
        Bundle result = new Bundle();
        result.putString(KEY_TARGET, target);
        result.putString(KEY_UNIT_NAME, unit.getName());
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
        dismiss();
    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int HEADER = 0;
        private static final int UNIT = 1;

        @Override
        public int getItemViewType(int position) {
            return rows.get(position) instanceof Unit ? UNIT : HEADER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            View v = inf.inflate(viewType == HEADER ? R.layout.item_picker_header : R.layout.item_unit_value,
                    parent, false);
            return new RecyclerView.ViewHolder(v) { };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object row = rows.get(position);
            View v = holder.itemView;
            Context c = v.getContext();
            if (row instanceof Unit.MeasureSystem) {
                ((TextView) v.findViewById(R.id.headerText)).setText(systemLabel(c, (Unit.MeasureSystem) row));
                return;
            }
            Unit u = (Unit) row;
            boolean isSelected = u.getName().equals(selected);
            TextView symbol = v.findViewById(R.id.rowSymbol);
            symbol.setText(u.getSymbol());
            symbol.setBackgroundTintList(ColorStateList.valueOf(ThemeManager.hue(c, category)));
            ((TextView) v.findViewById(R.id.rowName)).setText(u.getName());
            TextView note = v.findViewById(R.id.rowNote);
            note.setText(u.getNote());
            note.setVisibility(u.getNote().isEmpty() ? View.GONE : View.VISIBLE);
            TextView value = v.findViewById(R.id.rowValue);
            value.setText(isSelected ? "✓" : "");
            value.setTextColor(ThemeManager.color(c, R.attr.scAccent));
            v.setContentDescription(u.getName() + (isSelected ? ", " + c.getString(R.string.cd_selected) : ""));
            v.setOnClickListener(x -> pick(u));
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }
}
