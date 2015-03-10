package org.gvt.action;

import org.cbio.causality.analysis.RPPANetworkMapper;
import org.cbio.causality.model.RPPAData;
import org.cbio.causality.network.PhosphoSitePlus;
import org.cbio.causality.util.Histogram;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.RPPAWizard;

import java.util.List;

/**
 * @author Ozgun Babur
 */
public class LoadRPPAAction extends Action
{
	ChisioMain main;

	public LoadRPPAAction(ChisioMain main)
	{
		super("Load RPPA Data ...");
		this.main = main;
	}

	public void run()
	{
		try
		{
			// Load RPPA data

			RPPAWizard wizard = new RPPAWizard();
			WizardDialog dialog = new WizardDialog(main.getShell(), wizard);
			if (dialog.open() == 1) return;

			// Prepare network using the data

			List<RPPAData> datas = wizard.readData();
			PhosphoSitePlus.fillInMissingEffect(datas);

//			List<RPPAData> copy = RPPAData.copy(datas);
//			List<Integer> sizes = RPPANetworkMapper.getNullGraphSizes(copy, 1000, wizard.networkType.type);
//
//			Histogram h = new Histogram(1);
//			h.setBorderAtZero(true);
//			for (Integer size : sizes)
//			{
//				h.count(size);
//			}
//			h.print();

			double threshold = wizard.threshold;

			if (wizard.comparisonType == RPPAWizard.ComparisonType.TTEST ||
				wizard.valueMetric == RPPAWizard.ValueMetric.PVAL)
			{
				threshold = -Math.log(threshold) / Math.log(2);
			}

			RPPANetworkMapper.writeGraph(datas, threshold, wizard.getSIFFilename(),
				wizard.networkType.type, wizard.filterToGenes);

			LoadSIFFileAction action = new LoadSIFFileAction(main, wizard.getSIFFilename());
			action.run();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}