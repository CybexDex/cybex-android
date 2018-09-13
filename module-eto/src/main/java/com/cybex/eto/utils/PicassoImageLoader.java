package com.cybex.eto.utils;

import android.content.Context;
import android.widget.ImageView;

import com.cybex.provider.http.entity.EtoBanner;
import com.squareup.picasso.Picasso;
import com.youth.banner.loader.ImageLoader;

import java.util.Locale;

public class PicassoImageLoader extends ImageLoader {

    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        EtoBanner etoBanner = (EtoBanner) path;
        if(Locale.getDefault().getLanguage().equals("zh")){
            Picasso.get().load(etoBanner.getAdds_banner_mobile()).into(imageView);
        } else {
            Picasso.get().load(etoBanner.getAdds_banner_mobile__lang_en()).into(imageView);
        }
    }
}
