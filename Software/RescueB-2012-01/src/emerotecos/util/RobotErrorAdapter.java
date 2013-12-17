package emerotecos.util;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import emerotecos.rescue.b.R;

public class RobotErrorAdapter extends ArrayAdapter<RobotError> {

	ArrayList<RobotError> errors;
	LayoutInflater mInflater;

	public RobotErrorAdapter(Context context, ArrayList<RobotError> objects) {
		super(context, R.layout.item_error, objects);
		errors = objects;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public RobotError getItem(int position) {
		return errors.get(position);
	}
	
	public void setErrors(ArrayList<RobotError> errors) {
		this.errors = errors;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row;

		if (null == convertView) {
			row = mInflater.inflate(R.layout.item_error, null);
		} else {
			row = convertView;
		}

		TextView tError = (TextView) row.findViewById(R.id.item_error_error);
		TextView tDescription = (TextView) row.findViewById(R.id.item_error_description);
		ImageView icon = (ImageView) row.findViewById(R.id.item_error_icon);
		RobotError err = getItem(position);
		
		tError.setText(err.error);
		tDescription.setText("["+err.code+"] "+err.description);
		if(err.type == RobotError.E_OK){
			icon.setBackgroundResource(R.drawable.accept);
		}else if(err.type == RobotError.E_INFO){
			icon.setBackgroundResource(R.drawable.info);
		}else if(err.type == RobotError.E_ERROR){
			icon.setBackgroundResource(R.drawable.error);
		}
		
		return row;
	}

	@Override
	public int getCount() {
		return errors.size();
	}
}
