package janel.stormy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import janel.stormy.R;
import janel.stormy.weather_pkg.Hour;

public class HourAdapter extends BaseAdapter {

    private Context mContext;
    private Hour[] mHours;

    public HourAdapter(Context context, Hour[] hours) {
        mContext = context;
        mHours = hours;
    }

    @Override
    public int getCount() {
        return mHours.length;
    }

    @Override
    public Object getItem(int position) {
        return mHours[position];
    }

    @Override
    //won't use
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            //creating a new one
            convertView = LayoutInflater.from(mContext).inflate(R.layout.hourly_list_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView)convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView)convertView.findViewById(R.id.temperatureLabel);
            holder.hourLabel = (TextView)convertView.findViewById(R.id.hourLabel);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        Hour hour = mHours[position];

        holder.iconImageView.setImageResource(hour.getIconId());
        holder.temperatureLabel.setText(hour.getTemperature() + "");
        holder.hourLabel.setText(hour.getHourOfTheDay());

        return convertView;
    }

    private static class ViewHolder {
        ImageView iconImageView; //these are public by default
        TextView temperatureLabel;
        TextView hourLabel;
    }
}