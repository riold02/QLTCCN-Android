package com.example.qltccn.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.qltccn.R;

public class NotificationSettingsActivity extends AppCompatActivity {

    // UI elements
    private ImageButton toolbarBackBtn;
    private ImageView idNoti;
    private ImageView iconHome, iconChart, iconTrans, iconCategory, iconUser;
    
    private SwitchCompat switchGeneralNotification, switchSound, switchSilentCall, switchVibrate,
            switchTransactionUpdate, switchExpenseReminder, switchBudgetNotifications, switchLowBalanceAlerts;

    // Preference keys
    private static final String PREF_NAME = "notification_settings";
    private static final String KEY_GENERAL = "general_notification";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_SILENT_CALL = "silent_call";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_TRANSACTION = "transaction_update";
    private static final String KEY_EXPENSE = "expense_reminder";
    private static final String KEY_BUDGET = "budget_notifications";
    private static final String KEY_LOW_BALANCE = "low_balance_alerts";
    
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Initialize SharedPreferences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize UI elements
        initUI();

        // Load saved preferences
        loadSavedPreferences();

        // Set click listeners
        setClickListeners();
    }

    private void initUI() {
        // Toolbar
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        idNoti = findViewById(R.id.idNoti);

        // Switches
        switchGeneralNotification = findViewById(R.id.switchGeneralNotification);
        switchSound = findViewById(R.id.switchSound);
        switchSilentCall = findViewById(R.id.switchSilentCall);
        switchVibrate = findViewById(R.id.switchVibrate);
        switchTransactionUpdate = findViewById(R.id.switchTransactionUpdate);
        switchExpenseReminder = findViewById(R.id.switchExpenseReminder);
        switchBudgetNotifications = findViewById(R.id.switchBudgetNotifications);
        switchLowBalanceAlerts = findViewById(R.id.switchLowBalanceAlerts);

        // Footer icons
        iconHome = findViewById(R.id.iconHome);
        iconChart = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
    }

    private void loadSavedPreferences() {
        // Load saved switch states from SharedPreferences
        switchGeneralNotification.setChecked(preferences.getBoolean(KEY_GENERAL, true));
        switchSound.setChecked(preferences.getBoolean(KEY_SOUND, true));
        switchSilentCall.setChecked(preferences.getBoolean(KEY_SILENT_CALL, true));
        switchVibrate.setChecked(preferences.getBoolean(KEY_VIBRATE, true));
        switchTransactionUpdate.setChecked(preferences.getBoolean(KEY_TRANSACTION, true));
        switchExpenseReminder.setChecked(preferences.getBoolean(KEY_EXPENSE, false));
        switchBudgetNotifications.setChecked(preferences.getBoolean(KEY_BUDGET, true));
        switchLowBalanceAlerts.setChecked(preferences.getBoolean(KEY_LOW_BALANCE, false));
    }

    private void setClickListeners() {
        // Toolbar actions
        toolbarBackBtn.setOnClickListener(v -> finish());
        idNoti.setOnClickListener(v -> showNotificationInfo());

        // Switch change listeners
        switchGeneralNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_GENERAL, isChecked);
            updateDependentSwitches(isChecked);
        });
        
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_SOUND, isChecked));
        
        switchSilentCall.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_SILENT_CALL, isChecked));
        
        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_VIBRATE, isChecked));
        
        switchTransactionUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_TRANSACTION, isChecked));
        
        switchExpenseReminder.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_EXPENSE, isChecked));
        
        switchBudgetNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_BUDGET, isChecked));
        
        switchLowBalanceAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_LOW_BALANCE, isChecked));

        // Footer navigation
        iconHome.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, HomeActivity.class));
            finish();
        });
        
        iconChart.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, AnalysisActivity.class));
            finish();
        });
        
        iconTrans.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, TranActivity.class));
            finish();
        });
        
        iconCategory.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, CategoryActivity.class));
            finish();
        });
        
        iconUser.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, ProfileActivity.class));
            finish();
        });
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void updateDependentSwitches(boolean enabled) {
        // When general notifications are disabled, disable all other notification settings
        switchSound.setEnabled(enabled);
        switchSilentCall.setEnabled(enabled);
        switchVibrate.setEnabled(enabled);
        switchTransactionUpdate.setEnabled(enabled);
        switchExpenseReminder.setEnabled(enabled);
        switchBudgetNotifications.setEnabled(enabled);
        switchLowBalanceAlerts.setEnabled(enabled);
    }

    private void showNotificationInfo() {
        // TODO: Show information about notification settings
        // This would display a dialog with notification information
    }
} 