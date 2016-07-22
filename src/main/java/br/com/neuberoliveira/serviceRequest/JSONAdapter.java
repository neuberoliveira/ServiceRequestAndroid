package com.neuberdesigns.ServiceRequest;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * @author Neuber Oliveira
 */
public class JSONAdapter extends BaseAdapter {
    protected final static String LOG_ID = "JSONA";
    public ArrayList<JSONObject> list = new ArrayList<>();
    protected LayoutInflater inflater;
    protected int listViewId;
    protected String[] from;
    protected int[] to;
    protected String imageRegex = "";

    public JSONAdapter(Context context, JSONArray ja, int listViewId, String[] from, int to[]) {
        JSONObject json;
        this.listViewId = listViewId;
        this.from = from;
        this.to = to;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < ja.length(); i++) {
            try {
                json = ja.getJSONObject(i);
                list.add(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public JSONAdapter(Context context, JSONObject jo, int listViewId, String[] from, int to[]) {
        this(context, new JSONArray().put(jo), listViewId, from, to);
    }

    public static String getFromDot(String indexName, JSONObject json) {
        String[] props = indexName.split("\\.");
        String prop;
        String value = "";
        JSONObject jo = json;
        Object current;

        for (int i = 0; i < props.length; i++) {
            prop = props[i];
            try {
                current = jo.get(prop);
                if (current instanceof JSONObject) {
                    jo = jo.getJSONObject(prop);
                } else {
                    if (current instanceof JSONArray) {

                        boolean join = true;
                        int index;
                        try {
                            String nextProp = props[i + 1];
                            index = Integer.parseInt(nextProp);
                            jo = (JSONObject) jo.getJSONArray(nextProp).get(index);
                            i++;
                        } catch (NumberFormatException e) {
                            //Log.e(LOG_ID, "NAN: "+e.getMessage());
                        } catch (ArrayIndexOutOfBoundsException e) {
                            //Log.e(LOG_ID, "OFB: "+e.getMessage());
                        }

                        if (join && 0 > 10) {
                            value = jo.getJSONArray(prop).join(",");
                        } else {
                            value = jo.getString(prop);
                        }

                        value = value.replaceAll("(\\[(\"|“|”))|((\"|“|”)\\])", "");
                        value = value.replaceAll("(\\[\"|“)|(\"|”\\])", "");
                        value = value.replaceAll("(\", ?\")", ", ");
                    } else {
                        value = String.valueOf(jo.getString(prop));
                    }
                }
            } catch (JSONException e) {
                e.getStackTrace();
                //Log.d(LOG_ID, "all messed up: " + prop);
                //Log.e(LOG_ID, e.getMessage());
            }
        }

        if (value != null)
            value = Html.fromHtml(value).toString();

        //Log.d(LOG_ID, "Returned: "+value);
        return value;
    }

    public static String getPropertyFromDot(String indexName) {
        String[] props = indexName.split("\\.");

        return props[props.length - 1];
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<JSONObject> getList() {
        return list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseViewHolder viewHolder;
        int id;
        String prop, propSingle, value;
        TextView elementText;
        ImageView elementImage;
        JSONObject json = list.get(position);
        DownloadImageTask imageDownloader;
        View element;

        if (convertView == null) {
            convertView = inflater.inflate(listViewId, null);
            viewHolder = new ViewHolder();

            //Setting contents of View Holder
            for (int i = 0; i < from.length; i++) {
                elementImage = null;

                prop = from[i];
                propSingle = getPropertyFromDot(prop);
                id = to[i];
                value = getFromDot(prop, json);
                element = convertView.findViewById(id);

                try {
                    elementImage = (ImageView) element;
                } catch (ClassCastException e) {
                }

                if (elementImage != null) {
                    Log.d("value_holder", "EI: " + elementImage + " | F: " + propSingle);
                    viewHolder.set(propSingle, elementImage);
                    viewHolder.url = value;
                } else {
                    viewHolder.set(propSingle, (TextView) convertView.findViewById(id));
                }
            }
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Update list item with contents of the imageView holder
        for (int i = 0; i < from.length; i++) {
            prop = from[i];
            propSingle = getPropertyFromDot(prop);
            value = getFromDot(prop, json);

            if (isUrlImage(value) || viewHolder.get(propSingle) instanceof ImageView) {
                //Log.d("value_url", value);
                //Log.d("holder_url", viewHolder.url);
                elementImage = (ImageView) viewHolder.get(propSingle);
                if (!value.isEmpty())
                    new DownloadImageTask(elementImage).execute(value);
            } else {
                Log.d("prop_name", propSingle + " | " + isUrlImage(value));
                elementText = (TextView) viewHolder.get(propSingle);
                elementText.setText(value);
                elementText.setVisibility(elementText == null ? View.GONE : View.VISIBLE);
            }
        }

        return convertView;
    }

    public void setImageRegex(String expr) {
        imageRegex = expr;
    }

    public void refresh(JSONObject jo) {
        list.add(jo);
        notifyDataSetChanged();
    }

    protected boolean isUrlImage(String value) {
        String expr = imageRegex.isEmpty() ? "^https?://.*\\.(jpg|gif|png)$" : imageRegex;
        return isUrlImage(value, expr);
    }

    protected boolean isUrlImage(String value, String regex) {
        boolean result = value.matches(regex);

        return result;
    }

    static class ViewHolder extends BaseViewHolder {
        int position;
        String url;
        TextView id;
        TextView text;
    }
}
