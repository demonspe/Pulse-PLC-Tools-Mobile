package com.example.pulseplctoolsmobile;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pulseplctoolsmobile.enums.ImpNum;
import com.example.pulseplctoolsmobile.fragments.FragmentImpParams;
import com.example.pulseplctoolsmobile.fragments.FragmentLoadMonitor;
import com.example.pulseplctoolsmobile.fragments.FragmentMainParams;
import com.example.pulseplctoolsmobile.fragments.FragmentSearch;
import com.example.pulseplctoolsmobile.fragments.OnFragmentInteractionListener;
import com.example.pulseplctoolsmobile.fragments.Pages;
import com.example.pulseplctoolsmobile.link.LinkManager;
import com.example.pulseplctoolsmobile.link.OnLinkManagerInteractionListener;
import com.example.pulseplctoolsmobile.models.DeviceImpExParams;
import com.example.pulseplctoolsmobile.models.DeviceImpParams;
import com.example.pulseplctoolsmobile.models.DeviceMainParams;
import com.example.pulseplctoolsmobile.models.PulseBtDevice;
import com.example.pulseplctoolsmobile.models.PulsePLCv2LoginPass;
import com.example.pulseplctoolsmobile.protocol.AccessType;
import com.example.pulseplctoolsmobile.protocol.Commands;
import com.example.pulseplctoolsmobile.protocol.OnProtocolEvent;
import com.example.pulseplctoolsmobile.protocol.ProtocolDataContainer;
import com.example.pulseplctoolsmobile.protocol.ProtocolPulsePLCv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener, OnLinkManagerInteractionListener,
        OnMessageListener, OnProtocolEvent {

    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1;

    //Fragments
    FragmentSearch fSearch;
    FragmentMainParams fMainParams;
    FragmentImpParams fImp1, fImp2;
    FragmentLoadMonitor fLoadMonitor;

    //Статус Активити
    boolean isPaused;

    //Диалоговое окно прогресса
    private ProgressDialog progressDialog;

    //Pass
    String currentPass;

    //Pulse PLC Device
    PulseBtDevice currentDevice;

    //Link manager
    LinkManager linkManager;
    //Protocol for Pulse PLC v2
    ProtocolPulsePLCv2 protocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "На устройстве нет поддержки BLE", Toast.LENGTH_LONG).show();
            finish();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+ Permission APIs
            fuckMarshMallow();
        }

        progressDialog = new ProgressDialog(this);

        //Выбранное устройство
        currentDevice = null;

        //Init link manager
        linkManager = new LinkManager();
        linkManager.setListener(this);
        linkManager.setMessageListener(this);
        //Init protocol
        protocol = new ProtocolPulsePLCv2(linkManager);
        protocol.setOnMessageListener(this);
        protocol.setOnProtocolEvent(this);

        //Fragments
        fSearch = new FragmentSearch();
        fSearch.setListener(this);

        fMainParams = new FragmentMainParams();
        fMainParams.setListener(this);

        fImp1 = new FragmentImpParams();
        fImp1.setListener(this);
        fImp1.setCurrentImp(ImpNum.IMP1);

        fImp2 = new FragmentImpParams();
        fImp2.setListener(this);
        fImp2.setCurrentImp(ImpNum.IMP2);

        fLoadMonitor = new FragmentLoadMonitor();
        fLoadMonitor.setListener(this);

        //Открываем страницу поиска
        replaceFragment(fSearch, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        //Отвенить выполнение текущей команды
        //Таймер доступа(30сек) при этом не останавливается
        protocol.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    public void onDestroy() {
        //Отключиться если были подключенные устройства
        linkManager.disconnect();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            replaceFragment(fSearch, false);
        } else if (id == R.id.nav_main_params) {
            replaceFragment(fMainParams, false);
        } else if (id == R.id.nav_imp_1) {
            replaceFragment(fImp1, false);
        } else if (id == R.id.nav_imp_2) {
            replaceFragment(fImp2, false);
        } else if (id == R.id.nav_load_monitor) {
            replaceFragment(fLoadMonitor, false);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Перейти на страничку
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        if(addToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }
    //Go to page in App
    public void gotoPage(final Pages p) {
        if(p == Pages.Search)
            replaceFragment(fSearch, false);
        if(p == Pages.MainParams) {
            if(currentDevice != null)
                fMainParams.setCurrentDevice(currentDevice);
            replaceFragment(fMainParams, false);
        }
        if(p == Pages.Imp1)
            replaceFragment(fImp1, false);
        if(p == Pages.Imp2)
            replaceFragment(fImp2, false);
        if(p == Pages.Load)
            replaceFragment(fLoadMonitor, false);
    }
    //Выбрать устройство в качестве текущего
    public void setCurrentDevice(PulseBtDevice device) {
        if(currentDevice != null)
            if(!currentDevice.getFullName().equals(device.getFullName())) {
                fMainParams.resetFirstRead();
                //fImpParams.resetFirstRead();
            }
        currentDevice = device;
    }

    //Диалоговое окно подтверждения
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Отмена", null)
                .create()
                .show();
    }

    //Диалоговое окно запроса пароля
    private void showPassRequestDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите пароль");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password,
        // and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Авторизация", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //После нажатия "Ок" отправляем запрос на авторизацию
                currentPass = ""+input.getText().toString();
                PulsePLCv2LoginPass loginPass =
                        new PulsePLCv2LoginPass(currentDevice.getSerialNum(), currentPass);
                protocol.Send(Commands.Check_Pass, loginPass);
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                linkManager.disconnect();
            }
        });
        builder.show();
    }

    @Override
    public void onMessageShow(String text) {
        MsgOnUiThread(text);
    }

    @Override
    public void onScanBLERequest(boolean status) {
        if(status) linkManager.disconnect();
        linkManager.scanLeDevice(status);
    }

    @Override
    public void onConnectToDeviceRequest(PulseBtDevice device) {
        if(linkManager.getIsConnecting()) return;
        linkManager.disconnect();
        //Отобразим диалоговое окно подключения
        progressDialog.setMessage("Подключение к "+ device.getFullName());
        progressDialog.show();
        //Остановим сканирование
        linkManager.scanLeDevice(false);
        //Запомним текущее выбранное устройство
        setCurrentDevice(device);
        //Подключемся
        linkManager.connect(this, device);
    }

    @Override
    public void onGoToPageRequest(Pages p) {
        gotoPage(p);
    }

    @Override
    public void onSendDataDirectly(byte[] data) {
        linkManager.sendData(data);
        MsgOnUiThread("OUT: " + Helper.toStringHEX(data, ", "));
    }

    @Override
    public void onSendCmdRequest(Commands cmd, Object param) {
        if(linkManager.getIsConnected()){
            protocol.Send(cmd, param);
        }
        else {
            MsgOnUiThread("Подключение утеряно");
            gotoPage(Pages.Search);
        }
    }

    @Override
    public void onDataReceived(byte[] data) {
        protocol.DataReceived(data);
    }

    @Override
    public void onDeviceFound(PulseBtDevice pulseBtDevice) {
        fSearch.addDevice(pulseBtDevice);
    }

    @Override
    public void onConnectionSuccessful() {
        //Закроем диалоговое окно ожидания подключения
        if (progressDialog.isShowing()) progressDialog.dismiss();

        //После успешного подключения открываем окно ввода пароля
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showPassRequestDialog();
            }
        });
    }

    @Override
    public void onDisconnect() {
        //Закроем диалоговое окно ожидания подключения
        if (progressDialog.isShowing()) progressDialog.dismiss();
    }

    @Override
    public void onCommandEnd(ProtocolDataContainer dataContainer) {
//Some data from protocol
        Object data = dataContainer.Data;
        Commands cmd = dataContainer.CommandCode;

        if (cmd == Commands.Search_Devices) {
            //if(data != null) fragmentMainParams.setSerial((String)data);
        }
        if (cmd == Commands.Reboot) {
            linkManager.disconnect();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gotoPage(Pages.Search);
                }
            });
        }
        if (cmd == Commands.Check_Pass) {
            AccessType access = (AccessType) data;
            //Отобразить во View
        }
        if (cmd == Commands.Read_Main_Params) {
            final DeviceMainParams deviceMainParams = (DeviceMainParams)data;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fMainParams.setMainParams(deviceMainParams);
                }
            });
        }
        if (cmd == Commands.Read_IMP)
        {
            final DeviceImpParams imp = (DeviceImpParams)data;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(imp.getNum() == ImpNum.IMP1)
                        fImp1.setImpParams(imp);
                    if(imp.getNum() == ImpNum.IMP2)
                        fImp2.setImpParams(imp);
                }
            });
        }
        if (cmd == Commands.Read_IMP_extra)
        {
            final DeviceImpExParams impEx = (DeviceImpExParams)data;
            if(impEx != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { fLoadMonitor.addNewDataPoint(impEx);
                    }
                });
            }
        }
        /*if (cmd == PulsePLCv2Protocol.Commands.Read_DateTime)
        {
            Main_VM.Device.DeviceDateTime = (DateTime)data;
        }
        if (cmd == PulsePLCv2Protocol.Commands.Read_PLC_Table_En)
        {
            List<DataGridRow_PLC> rows = (List<DataGridRow_PLC>)data;
            PLCTable_VM.TablePLC.ToList().ForEach(item => item.IsEnable = false);
            foreach (var item in rows) PLCTable_VM.TablePLC[item.Adrs_PLC - 1].IsEnable = true;
            var rowsToRead = rows.Split(10); //Divide into groups of 10 item
            //Send requests for data
            rowsToRead.ForEach(r => {
            if (r.Count > 0)
                CommandManager.Add_CMD(LinkManager.Link, Protocol, PulsePLCv2Protocol.Commands.Read_PLC_Table, r, 0);
            });
            CommandManager.Add_CMD(LinkManager.Link, Protocol, PulsePLCv2Protocol.Commands.Close_Session, null, 0);
        }
        if (cmd == PulsePLCv2Protocol.Commands.Read_PLC_Table)
        {
            var rows = (List<DataGridRow_PLC>)data;
            foreach (var item in rows) PLCTable_VM.TablePLC[item.Adrs_PLC - 1] = item;
        }
        if (cmd == PulsePLCv2Protocol.Commands.Read_E_Current)
        {
            var row = (DataGridRow_PLC)data;
            PLCTable_VM.TablePLC[row.Adrs_PLC - 1].E_Current = row.E_Current;
        }
        if (cmd == PulsePLCv2Protocol.Commands.Read_E_Start_Day)
        {
            var row = (DataGridRow_PLC)data;
            PLCTable_VM.TablePLC[row.Adrs_PLC - 1].E_StartDay = row.E_StartDay;
        }
        if (cmd == PulsePLCv2Protocol.Commands.Read_Journal)
        {
            var journal = (JournalForProtocol)data;
            switch (journal.Type)
            {
                case Journal_type.POWER:
                    Main_VM.JournalPower.Clear();
                    journal.Events.ForEach(row => Main_VM.JournalPower.Add(row));
                    break;
                case Journal_type.CONFIG:
                    Main_VM.JournalConfig.Clear();
                    journal.Events.ForEach(row => Main_VM.JournalConfig.Add(row));
                    break;
                case Journal_type.INTERFACES:
                    Main_VM.JournalInterfaces.Clear();
                    journal.Events.ForEach(row => Main_VM.JournalInterfaces.Add(row));
                    break;
                case Journal_type.REQUESTS:
                    Main_VM.JournalRequestsPLC.Clear();
                    journal.Events.ForEach(row => Main_VM.JournalRequestsPLC.Add(row));
                    break;
                default:
                    break;
            }

        }*/
    }

    @Override
    public void onAccessEnd() {
        //TODO придумать что-то нормальное (30 секунд мало)
        if(isPaused) {
            linkManager.disconnect();
        }
        else
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gotoPage(Pages.Search);
                }
            });
    }

    @Override
    public void onAccessGranted(AccessType accessType) {
        //После успешной авторизации, переходим к основным параметрам
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gotoPage(Pages.MainParams);
            }
        });
    }

    //region Отображение всплывающего сообщения
    void Msg(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    void MsgOnUiThread(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Msg(text);
            }
        });
    }
    //endregion

    //region Разрешения для Bluetooth
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                    // Permission Denied
                    Toast.makeText(this, "Все разрешения получены! Спасибо :)", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Не все разрешения получены :(", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fuckMarshMallow() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {

                // Need Rationale
                String message = "App need access to " + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        Toast.makeText(this, "Привет", Toast.LENGTH_SHORT)
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }
    //endregion
}
