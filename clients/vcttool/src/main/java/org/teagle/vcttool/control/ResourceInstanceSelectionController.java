/**
 * 
 */
package org.teagle.vcttool.control;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.teagle.clients.api.LegacyTeagleClient;

import teagle.vct.model.ResourceInstance;
import teagle.vct.model.ResourceInstanceState;

/**
 * @author sim
 * 
 */
public class ResourceInstanceSelectionController {

	private static Map<String, ResourceInstanceController> controlers = new HashMap<String, ResourceInstanceController>();

	private final Tree tree;
	private final LegacyTeagleClient teagleClient;

	public ResourceInstanceSelectionController(final RootController root,
			final String username, final Composite parent) {
		this.teagleClient = new LegacyTeagleClient(root.getConfig().getUsername(),
				root.getConfig().getPassword(), root.getConfig()
						.getReqprocUrl(), root.getConfig().getRepoUrl());

		this.tree = new Tree(parent, SWT.NONE);
		this.initDragDrop();
		this.init();
	}

	private void initDragDrop() {
		final DragSource dragSource = new DragSource(this.tree, DND.DROP_COPY
				| DND.DROP_MOVE);
		final Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		dragSource.setTransfer(types);
		dragSource.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(final DragSourceEvent event) {
				// Object data = tree.getSelection()[0].getData();
				final String transfer = "instance:"
						+ ((ResourceInstance) ResourceInstanceSelectionController.this.tree
								.getSelection()[0].getData()).getCommonName();
				event.data = transfer;
			}
		});
	}

	private void init() {
		final Collection<ResourceInstance> instances = this.teagleClient
				.getResourceInstances();

		for (final ResourceInstance instance : instances) {
			ResourceInstanceSelectionController.controlers.put(instance
					.getCommonName(), new ResourceInstanceController(instance));
			if (instance.getState() == ResourceInstanceState.State.PROVISIONED) {
				final TreeItem instanceItem = new TreeItem(this.tree, SWT.NONE);
				instanceItem.setText(instance.getCommonName());
				instanceItem.setData(instance);
			}
		}

	}

	public void addResourceInstanceControler(
			final ResourceInstanceController controler) {
		ResourceInstanceSelectionController.controlers.put(controler
				.getResourceInstance().getCommonName(), controler);
	}

	public ResourceInstanceController removeResourceInstanceControler(
			final String commonName) {
		return ResourceInstanceSelectionController.controlers
				.remove(commonName);
	}

	public static ResourceInstanceController findResourceInstanceController(
			final String commonName) {
		return ResourceInstanceSelectionController.controlers.get(commonName);
	}

	public Control getSelectionView() {
		return this.tree;
	}

	public void reload() {
		ResourceInstanceSelectionController.controlers.clear();
		this.tree.removeAll();
		this.init();
	}

}
