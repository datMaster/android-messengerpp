package org.solovyev.android.messenger.http;

import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.http.CachingImageLoader;

/**
 * User: serso
 * Date: 8/12/12
 * Time: 10:43 PM
 */
@Singleton
public class MessengerRemoteFileService extends CachingImageLoader {

    @Inject
    public MessengerRemoteFileService(@NotNull Context context) {
        super(context, "messenger");
    }
}
