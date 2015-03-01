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

import griffon.core.artifact.GriffonController;
import griffon.metadata.ArtifactProviderFor;

import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController;

@ArtifactProviderFor(GriffonController.class)
public class CdtoolController extends AbstractGriffonController {
	private CdtoolModel model;

	public void setModel(CdtoolModel model) {
		this.model = model;
	}

	public void click() {
		runInsideUIAsync(new Runnable() {
			@Override
			public void run() {
				System.out.println("Click");
				int count = Integer.parseInt(model.getClickCount());
				model.setClickCount(String.valueOf(count + 1));
			}
		});
	}
}