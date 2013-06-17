package org.gvt;

import org.eclipse.gef.requests.CreationFactory;
import org.gvt.model.NodeModel;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ModelFactory implements CreationFactory {
	private String path;

	public Object getNewObject() {
		NodeModel model = new NodeModel();
		model.setText(path);
		return model;
	}

	public Object getObjectType() {
		return NodeModel.class;
	}

	public void setPath(String s) {
		path = s;
	}
}
