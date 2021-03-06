/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.express.internal.ui.wizard.domain;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.common.ui.databinding.ParametrizableWizardPageSupport;
import org.jboss.tools.common.ui.databinding.ValueBindingBuilder;
import org.jboss.tools.openshift.express.internal.ui.databinding.RequiredControlDecorationUpdater;
import org.jboss.tools.openshift.express.internal.ui.utils.StringUtils;
import org.jboss.tools.openshift.express.internal.ui.wizard.AbstractOpenShiftWizardPage;

/**
 * @author André Dietisheim
 */
public class EditDomainWizardPage extends AbstractOpenShiftWizardPage {

	private EditDomainWizardPageModel pageModel;

	public EditDomainWizardPage(EditDomainWizardPageModel model, IWizard wizard) {
		super("OpenShift Domain Edition", "Rename your domain", "Domain Name Edition", wizard);
		this.pageModel = model;
	}

	protected void doCreateControls(Composite container, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
		createDomainGroup(container, dbc);
	}

	private void createDomainGroup(Composite container, DataBindingContext dbc) {
		Composite domainGroup = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).span(2, 1).applyTo(domainGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(4).applyTo(domainGroup);
		Label namespaceLabel = new Label(domainGroup, SWT.NONE);
		namespaceLabel.setText("&Domain name");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(namespaceLabel);
		Text namespaceText = new Text(domainGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(namespaceText);
		ISWTObservableValue namespaceTextObservable = WidgetProperties.text(SWT.Modify)
				.observe(namespaceText);
		final NamespaceValidator namespaceValidator = new NamespaceValidator(namespaceTextObservable);
		dbc.addValidationStatusProvider(namespaceValidator);
		ControlDecorationSupport.create(namespaceValidator, SWT.LEFT | SWT.TOP, null,
				new RequiredControlDecorationUpdater());
		final IObservableValue namespaceModelObservable = BeanProperties.value(
				EditDomainWizardPageModel.PROPERTY_DOMAIN_ID).observe(pageModel);
		ValueBindingBuilder.bind(namespaceTextObservable).to(namespaceModelObservable).in(dbc);
	}

	@Override
	protected void setupWizardPageSupport(DataBindingContext dbc) {
		ParametrizableWizardPageSupport.create(IStatus.ERROR | IStatus.CANCEL, this, dbc);
	}
	
	class NamespaceValidator extends MultiValidator {

		private final ISWTObservableValue domainNameObservable;

		public NamespaceValidator(ISWTObservableValue domainNameObservable) {
			this.domainNameObservable = domainNameObservable;
		}

		@Override
		protected IStatus validate() {
			final String domainName = (String) domainNameObservable.getValue();
			if (pageModel.isCurrentDomainId(domainName)) {
				return ValidationStatus.cancel(getDescription());
			}
			if (domainName.isEmpty()) {
				return ValidationStatus.cancel(
						"Enter a domain name with lower-case letters and digits only. Max length is 16 characters.");
			}
			if (!StringUtils.isAlphaNumeric(domainName)) {
				return ValidationStatus.error(
						"The domain name may only contain lower-case letters and digits.");
			}
			if (domainName.length() > 16) {
				return ValidationStatus.error(
						"The domain name max length is 16 characters.");
			}
			return ValidationStatus.ok();
		}

		@Override
		public IObservableList getTargets() {
			WritableList targets = new WritableList();
			targets.add(domainNameObservable);
			return targets;
		}
	}

}
