package com.taibah.busservice.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.taibah.busservice.MainActivity;
import com.taibah.busservice.R;
import com.taibah.busservice.utils.AppGlobals;
import com.taibah.busservice.utils.DriverService;
import com.taibah.busservice.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment implements View.OnClickListener {

    View convertView;
    Button buttonReportSituation;
    Button buttonStartStopRoute;
    RadioGroup radioGroupReportSituation;
    RelativeLayout layoutDriverButtons;
    RelativeLayout layoutRouteCancelled;
    RelativeLayout layoutRouteInfo;
    LinearLayout layoutAdminInfo;
    TextView tvUserType;
    TextView tvRouteStatus;
    TextView tvRouteClickToRestore;
    TextView tvRouteArrivalTime;
    TextView tvRouteDepartureTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.layout_home, null);

        tvUserType = (TextView) convertView.findViewById(R.id.tv_user_type);
        tvRouteStatus = (TextView) convertView.findViewById(R.id.tv_route_status);
        tvRouteArrivalTime = (TextView) convertView.findViewById(R.id.tv_arrival_time);
        tvRouteDepartureTime = (TextView) convertView.findViewById(R.id.tv_departure_time);
        tvRouteClickToRestore = (TextView) convertView.findViewById(R.id.tv_route_click_to_restore);
        layoutDriverButtons = (RelativeLayout) convertView.findViewById(R.id.layout_driver_buttons);
        layoutAdminInfo = (LinearLayout) convertView.findViewById(R.id.layout_admin_info);
        layoutRouteCancelled = (RelativeLayout) convertView.findViewById(R.id.layout_driver_route_cancelled);
        layoutRouteCancelled.setOnClickListener(this);

        layoutRouteInfo = (RelativeLayout) convertView.findViewById(R.id.layout_route_info);

        buttonStartStopRoute = (Button) convertView.findViewById(R.id.btn_route_switch);
        buttonStartStopRoute.setOnClickListener(this);

        buttonReportSituation = (Button) convertView.findViewById(R.id.btn_report_situation);
        buttonReportSituation.setOnClickListener(this);

        setAppView();
        setRouteStatus(AppGlobals.getRouteStatus());

        if (AppGlobals.getUserType() != 0 && AppGlobals.getRouteStatus() < 2) {
            try {
                JSONObject jsonObject = new JSONObject(AppGlobals.getStudentDriverRouteID());
                String arrivalTime = jsonObject.getString("arrival_time");
                String departureTime = jsonObject.getString("departure_time");
                tvRouteArrivalTime.setText(arrivalTime.substring(arrivalTime.length() - 8));
                tvRouteDepartureTime.setText(departureTime.substring(departureTime.length() - 8));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }

    private void setRouteStatus(int status) {
        if (status == 0) {
            if (AppGlobals.getUserType() == 2) {
                layoutDriverButtons.setVisibility(View.VISIBLE);
            }
            layoutRouteCancelled.setVisibility(View.GONE);
            if (AppGlobals.getUserType() != 0) {
                layoutRouteInfo.setVisibility(View.VISIBLE);
            }
        } else {
            if (AppGlobals.getUserType() == 2) {
                layoutDriverButtons.setVisibility(View.GONE);
            }
            layoutRouteCancelled.setVisibility(View.VISIBLE);
            if (AppGlobals.getUserType() != 0) {
                layoutRouteInfo.setVisibility(View.GONE);
                if (status == 1) {
                    tvRouteStatus.setText("Accident");
                } else if (status == 2) {
                    tvRouteStatus.setText("Driver unavailable");
                } else if (status == 3) {
                    tvRouteStatus.setText("Bus out of service");
                }
            }
        }
    }

    public void setAppView() {
        if (AppGlobals.getUserType() == 2) {
            layoutDriverButtons.setVisibility(View.VISIBLE);
            tvUserType.setText("UserType: Driver");
            tvRouteClickToRestore.setVisibility(View.VISIBLE);
            layoutAdminInfo.setVisibility(View.GONE);
            if (DriverService.driverLocationReportingServiceIsRunning) {
                buttonStartStopRoute.setText("End Route");
            } else {
                buttonStartStopRoute.setText("Start Route");
            }
        } else if (AppGlobals.getUserType() == 1) {
            layoutDriverButtons.setVisibility(View.GONE);
            tvUserType.setText("UserType: Student");
            layoutAdminInfo.setVisibility(View.GONE);
            layoutRouteCancelled.setClickable(false);
        } else if (AppGlobals.getUserType() == 0) {
            layoutDriverButtons.setVisibility(View.GONE);
            layoutAdminInfo.setVisibility(View.VISIBLE);
            tvUserType.setText("UserType: Admin");
            layoutRouteCancelled.setClickable(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_route_switch:
                if (Helpers.isNetworkAvailable()) {
                    if (Helpers.isHighAccuracyLocationServiceAvailable()) {
                        if (!DriverService.driverLocationReportingServiceIsRunning) {
                            AlertDialog.Builder alertDialogRouteSwitch = new AlertDialog.Builder(
                                    getActivity());
                            alertDialogRouteSwitch.setTitle("Start Route");
                            alertDialogRouteSwitch
                                    .setMessage("Are you sure?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            Helpers.showProgressDialog(getActivity(), "Starting Route");

                                            // TODO: Implement route starting logic here.

                                            new android.os.Handler().postDelayed(
                                                    new Runnable() {
                                                        public void run() {
                                                            getActivity().startService(new Intent(getActivity(), DriverService.class));
                                                            buttonStartStopRoute.setText("End Route");
                                                            Helpers.dismissProgressDialog();
                                                            AppGlobals.replaceFragment(getFragmentManager(), new MapsFragment());

                                                            MainActivity.navigationView.getMenu().getItem(1).setChecked(true);
                                                        }
                                                    }, 2000);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog routeSwitchDialog = alertDialogRouteSwitch.create();
                            routeSwitchDialog.show();
                        } else {
                            AlertDialog.Builder alertDialogRouteSwitch = new AlertDialog.Builder(
                                    getActivity());
                            alertDialogRouteSwitch.setTitle("End Route");
                            alertDialogRouteSwitch
                                    .setMessage("Are you sure?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            Helpers.showProgressDialog(getActivity(), "Ending Route");

                                            DriverService.isRouteCancelled = true;
                                            DriverService.onLocationChangedCounter = 0;

                                            new android.os.Handler().postDelayed(
                                                    new Runnable() {
                                                        public void run() {
                                                            buttonStartStopRoute.setText("Start Route");
                                                            Helpers.dismissProgressDialog();
                                                            getActivity().stopService(new Intent(getActivity(), DriverService.class));
                                                        }
                                                    }, 2000);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog routeSwitchDialog = alertDialogRouteSwitch.create();
                            routeSwitchDialog.show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error: Location Service not on HighAccuracy", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Error: Not connected to the network", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.layout_driver_route_cancelled:
                if (Helpers.isNetworkAvailable()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getActivity());
                    alertDialogBuilder.setTitle("Restore Route");
                    alertDialogBuilder
                            .setMessage("Are you sure?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (Helpers.isNetworkAvailable()) {

                                        Helpers.showProgressDialog(getActivity(), "Restoring Route");

                                        // TODO: Implement restoration logic here.

                                        new android.os.Handler().postDelayed(
                                                new Runnable() {
                                                    public void run() {
                                                        AppGlobals.putRouteStatus(0);
                                                        setRouteStatus(0);
                                                        Helpers.dismissProgressDialog();
                                                    }
                                                }, 2000);
                                    } else {
                                        Toast.makeText(getActivity(), "Not connected to the network", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog restoreRouteDialog = alertDialogBuilder.create();
                    restoreRouteDialog.show();
                } else {
                    Toast.makeText(getActivity(), "Error: Not connected to the network", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_report_situation:
                if (Helpers.isNetworkAvailable()) {
                    if (!DriverService.driverLocationReportingServiceIsRunning) {
                        final Dialog reportSituationDialog = new Dialog(getActivity());
                        reportSituationDialog.setContentView(R.layout.layout_report_dialog);
                        reportSituationDialog.setTitle("Choose a situation");
                        reportSituationDialog.setCancelable(false);

                        radioGroupReportSituation = (RadioGroup) reportSituationDialog.findViewById(R.id.rg_report_situation);

                        Button dialogButtonCancel = (Button) reportSituationDialog.findViewById(R.id.btn_report_dialog_cancel);
                        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                reportSituationDialog.dismiss();
                            }
                        });

                        Button dialogButtonOk = (Button) reportSituationDialog.findViewById(R.id.btn_report_dialog_ok);
                        dialogButtonOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Helpers.isNetworkAvailable()) {

                                    reportSituationDialog.dismiss();

                                    Helpers.showProgressDialog(getActivity(), "Reporting situation");

                                    int id = radioGroupReportSituation.getCheckedRadioButtonId();
                                    View radioButton = radioGroupReportSituation.findViewById(id);
                                    final int radioIndex = radioGroupReportSituation.indexOfChild(radioButton) + 1;
                                    Log.i("BusService", "SituationReportingIndex: " + radioIndex);

                                    // TODO: Implement reporting logic here.

                                    new android.os.Handler().postDelayed(
                                            new Runnable() {
                                                public void run() {
                                                    AppGlobals.putRouteStatus(radioIndex);
                                                    setRouteStatus(radioIndex);
                                                    Helpers.dismissProgressDialog();
                                                }
                                            }, 2000);
                                } else {
                                    Toast.makeText(getActivity(), "Not connected to the network", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        reportSituationDialog.show();
                    } else {
                        Toast.makeText(getActivity(), "Error: Driver Service is running", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Error: Not connected to the network", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.isHomeFragmentOpen = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.isHomeFragmentOpen = false;
    }
}