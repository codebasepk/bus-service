package com.byteshaft.busservice.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.byteshaft.busservice.R;
import com.byteshaft.busservice.utils.Helpers;

public class RegisterStudent extends Fragment {
    View convertView;

    public static EditText etStudentFirstName;
    public static EditText etStudentLastName;
    public static EditText etStudentContactNumber;
    public static EditText etStudentRollNumber;
    public static EditText etStudentEmail;

    String firstNameStudent;
    String lastNameStudent;
    String contactNumberStudent;
    String rollNumberStudent;
    String emailStudent;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.layout_register_student, null);
        setHasOptionsMenu(true);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) convertView.findViewById(R.id.container_student);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) convertView.findViewById(R.id.tabs_student);
        tabLayout.setupWithViewPager(mViewPager);

        return convertView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_done, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done_button:

                firstNameStudent =  etStudentFirstName.getText().toString().trim();
                lastNameStudent = etStudentLastName.getText().toString().trim();
                contactNumberStudent = etStudentContactNumber.getText().toString().trim();
                rollNumberStudent = etStudentRollNumber.getText().toString().trim();
                emailStudent = etStudentEmail.getText().toString().trim();

                register();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Info";
                case 1:
                    return "Stop";
            }
            return null;
        }
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int tabCount =  getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;
            if (tabCount == 1) {
                rootView = inflater.inflate(R.layout.layout_register_student_info, container, false);

                etStudentFirstName = (EditText) rootView.findViewById(R.id.et_student_first_name);
                etStudentLastName = (EditText) rootView.findViewById(R.id.et_student_last_name);
                etStudentContactNumber = (EditText) rootView.findViewById(R.id.et_student_contact);
                etStudentRollNumber = (EditText) rootView.findViewById(R.id.et_student_roll_number);
                etStudentEmail = (EditText) rootView.findViewById(R.id.et_student_email);

            } else if (tabCount == 2) {
                rootView = inflater.inflate(R.layout.layout_register_student_route, container, false);
            }
            return rootView;
        }
    }

    public void register() {

        if (!validate()) {
            onRegistrationFailed();
            return;
        }

        String username = "sdt" + firstNameStudent + rollNumberStudent.substring(rollNumberStudent.length() - 3);
        String password = lastNameStudent + rollNumberStudent.substring(rollNumberStudent.length() - 3 );

        Log.i("username", " " + username);
        Log.i("password", " " + password);

        Helpers.showProgressDialog(getActivity(), "Registering");

        // TODO: Implement registration here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        onRegistrationSuccess();
                        Helpers.dismissProgressDialog();
                    }
                }, 2000);
    }

    public boolean validate() {
        boolean valid = true;

        if (firstNameStudent.isEmpty() || firstNameStudent.length() < 3) {
            etStudentFirstName.setError("at least 3 characters");
            valid = false;
        } else {
            etStudentFirstName.setError(null);
        }

        if (lastNameStudent.isEmpty() || lastNameStudent.length() < 3) {
            etStudentLastName.setError("at least 3 characters");
            valid = false;
        } else {
            etStudentLastName.setError(null);
        }

        if (rollNumberStudent.isEmpty() || rollNumberStudent.length() < 3) {
            etStudentRollNumber.setError("at least 3 characters");
            valid = false;
        } else {
            etStudentContactNumber.setError(null);
        }

        if (!contactNumberStudent.isEmpty() && !PhoneNumberUtils.isGlobalPhoneNumber(contactNumberStudent)) {
            etStudentContactNumber.setError("Number is invalid");
            valid = false;
        } else {
            etStudentContactNumber.setError(null);
        }

        if (!emailStudent.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStudent).matches()) {
            etStudentEmail.setError("Email is invalid");
            valid = false;
        } else {
            etStudentEmail.setError(null);
        }

        Log.i("Status", "Valid" + valid);
        return valid;
    }

    public void onRegistrationSuccess() {
        Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_SHORT).show();
        Helpers.closeKeyboard(getActivity(), etStudentContactNumber.getWindowToken());
        getActivity().onBackPressed();
    }

    public void onRegistrationFailed() {
        Toast.makeText(getActivity(), "Registration failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("OnResume", "OnResume");

    }
}


