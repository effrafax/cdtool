package mst.cdtool;

import griffon.core.GriffonApplication;
import griffon.core.artifact.GriffonController;
import griffon.metadata.ArtifactProviderFor;
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import griffon.transform.Threading;

@ArtifactProviderFor(GriffonController.class)
public class CdtoolController extends AbstractGriffonController {
    private CdtoolModel model;

    @Inject
    public CdtoolController(@Nonnull GriffonApplication application) {
        super(application);
    }

    public void setModel(CdtoolModel model) {
        this.model = model;
    }

    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    public void click() {
        int count = Integer.parseInt(model.getClickCount());
        model.setClickCount(String.valueOf(count + 1));
    }
}