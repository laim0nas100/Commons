package com.github.laim0nas100.commonslb.javafx.scenemanagement.frames;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import com.github.laim0nas100.commonslb.iteration.streams.MakeStream;

/**
 * Set custom stylesheet for stage.
 *
 * @author laim0nas100
 */
public class WithStylesheet extends WithDefaultStageProperties {

    public WithStylesheet(URL... urls) {
        this(Arrays.asList(urls));
    }

    public WithStylesheet(Collection<URL> urls) {
        super(st -> {
            st.getScene()
                    .getStylesheets()
                    .addAll(
                            MakeStream.from(urls)
                                    .map(url -> url.toExternalForm())
                                    .toList()
                    );
        });
    }

}
