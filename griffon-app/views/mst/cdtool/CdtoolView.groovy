/*
 * Copyright 2015 Martin Stockhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mst.cdtool;

import griffon.core.artifact.GriffonView;
import griffon.metadata.ArtifactProviderFor;

import java.util.Collections;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.codehaus.griffon.runtime.javafx.artifact.AbstractJavaFXGriffonView;

@ArtifactProviderFor(GriffonView.class)
public class CdtoolView extends AbstractJavaFXGriffonView {
    private CdtoolController controller;
    private CdtoolModel model;

	
    @FXML
    private Label counterLabel;
    

    public void setController(CdtoolController controller) {
        this.controller = controller;
    }

    public void setModel(CdtoolModel model) {
        this.model = model;
    }

    @Override
    public void initUI() {
        Stage stage = (Stage) getApplication()
            .createApplicationContainer(Collections.<String,Object>emptyMap());
        stage.setTitle(getApplication().getConfiguration().getAsString("application.title"));
        stage.setScene(init());
        stage.sizeToScene();
        getApplication().getWindowManager().attach("mainWindow", stage);
    }

    // build the UI
    private Scene init() {
        Node node = loadFromFXML();
        Scene scene = new Scene((Parent) node, 800,600);
        scene.setFill(Color.WHITE);
		
        model.clickCountProperty().bindBidirectional(counterLabel.textProperty());
        
		connectActions(node, controller);
        return scene;
    }
}
