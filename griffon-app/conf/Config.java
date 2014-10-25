import griffon.util.AbstractMapResourceBundle;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.util.Arrays.asList;
import static griffon.util.CollectionUtils.map;

public class Config extends AbstractMapResourceBundle {
    @Override
    protected void initialize(@Nonnull Map<String, Object> entries) {
        map(entries)
            .e("application", map()
                .e("title", "cdtool")
                .e("startupGroups", asList("cdtool"))
                .e("autoShutdown", true)
            )
            .e("mvcGroups", map()
                .e("cdtool", map()
                    .e("model", "mst.cdtool.CdtoolModel")
                    .e("view", "mst.cdtool.CdtoolView")
                    .e("controller", "mst.cdtool.CdtoolController")
                )
            );
    }
}