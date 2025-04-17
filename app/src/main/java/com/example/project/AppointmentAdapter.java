package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class AppointmentAdapter extends ArrayAdapter<Appointment> {
    private Context context;
    private List<Appointment> appointments;

    public AppointmentAdapter(Context context, List<Appointment> appointments) {
        super(context, R.layout.appointment_item, appointments);
        this.context = context;
        this.appointments = appointments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.appointment_item, parent, false);
        }

        Appointment appointment = appointments.get(position);

        TextView tvCustomerName = convertView.findViewById(R.id.tv_customer_name);
        TextView tvDate = convertView.findViewById(R.id.tv_date);
        TextView tvTime = convertView.findViewById(R.id.tv_time);

        tvCustomerName.setText("לקוח: " + appointment.getCustomerName());
        tvDate.setText("תאריך: " + appointment.getDate());
        tvTime.setText("שעה: " + appointment.getTime());

        return convertView;
    }
} 