package org.patika.mada.util;

import org.patika.mada.dataXML.*;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * This class manages the loaded microarray data to the client. A manager per
 * aplication exists. This is initialized in parallel with <code>ChisioMain</code>
 * class.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExperimentDataManager
{
	/**
	 *    This is the microarray data object
	 */
	private ChisioExperimentData ced;

	/**
	 * Type of experiment data. Can be MICROARRAY or MASS_SPECT.
	 */
	private String type;

	/**
	 * For saving user grouping information.
	 */
	private String fileLocation;

	/**
	 * Holds hash maps that maps genes to array data.
	 */
	private List<Map<XRef, List<Double>>> experimentMapsList;

	/**
	 * Information for each experiment.
	 */
	private List<String> experimentInfoList;

	/**
	 * Names of the experiments.
	 */
	private List<String> experimentNameList;

	/**
	 * Maximum values that is present in each experiment.
	 */
	private List<Double> maxValueList;

	/**
	 * Minimum values that is present in each experiment.
	 */
	private List<Double> minValueList;

	/**
	 * Info for all experiments.
	 */
	private String datasetInfo;

	/**
	 * Indexes of the first (or the) group of experiments to be used on the
	 * graph.
	 */
	private List<Integer> firstExpIndices;

	/**
	 * Index of the second (if used) group of experiments to be used on the
	 * graph.
	 */
	private List<Integer> secondExpIndices;

	/**
	 * Determines the method to calculate a value from a set of experiment
	 * values. Possible values are MEAN and MEDIAN.
	 */
	private int averaging;

	/**
	 * Constructor.
	 *
	 * @param type type of the experiment data, MICROARRAY or MASS_SPEC
	 */
	public ExperimentDataManager(String type)
	{
		this.type = type;

		this.firstExpIndices = new ArrayList<Integer>();
		this.secondExpIndices = new ArrayList<Integer>();

		this.averaging = MAX;
	}

	/**
	 * Constructor.
	 *
	 * @param data the experiment data to manage
	 */
	public ExperimentDataManager(ChisioExperimentData data, String fileLocation)
	{
		this(data.getExperimentType());
		setData(data);
		this.fileLocation = fileLocation;
	}

	/**
	 * @return type of the experiment that is managed
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets the microarray data to be used.
	 * @param data to set
	 */
	public void setData(ChisioExperimentData data)
	{
		this.firstExpIndices.clear();
		this.secondExpIndices.clear();

		this.ced = data;

		int expSize = data.getExperiment().size();

		Map<Integer, Integer> expNoToIndex = new HashMap<Integer, Integer>();

		this.maxValueList = new ArrayList<Double>(expSize);
		this.minValueList = new ArrayList<Double>(expSize);

		for (int i = 0; i < expSize; i++)
		{
			maxValueList.add(-Double.MAX_VALUE);
			minValueList.add( Double.MAX_VALUE);
		}

		Iterator expIter = data.getExperiment().iterator();

		boolean hasExpInfo = ((Experiment) data.getExperiment().get(0)).
			getExperimentInfo() != null;

		if (hasExpInfo)
		{
			this.experimentInfoList = new ArrayList<String>(expSize);
		}

		this.experimentNameList = new ArrayList<String>(expSize);

		for (int i = 0; i < expSize; i++)
		{
			Experiment exp = (Experiment) expIter.next();

			assert !expNoToIndex.containsKey(exp.getNo()) :
				"Multiple experiments with same No: " + exp.getNo();

			expNoToIndex.put(exp.getNo(), i);

			if (hasExpInfo) this.experimentInfoList.add(exp.getExperimentInfo());
			this.experimentNameList.add(exp.getExperimentName());
		}

		this.datasetInfo = data.getExperimentSetInfo();

		this.experimentMapsList = new ArrayList<Map<XRef, List<Double>>>(expSize);

		for (int i = 0; i < expSize; i++)
		{
			this.experimentMapsList.add(new HashMap<XRef, List<Double>>());
		}

		for (Object o1 : data.getRow())
		{
			Row row = (Row) o1;

			for (Object o2 : row.getValue())
			{
				ValueTuple val = (ValueTuple) o2;

				double value = val.getValue();

				int expIndex = expNoToIndex.get(val.getNo());

				Map<XRef, List<Double>> map = this.experimentMapsList.get(expIndex);

				for (Object o3 : row.getRef())
				{
					XRef ref = new XRef((Reference) o3);

					if (!map.containsKey(ref))
					{
						map.put(ref, new ArrayList<Double>());
					}

					map.get(ref).add(value);

					if (maxValueList.get(expIndex) < value)
					{
						maxValueList.set(expIndex, value);
					}
					if (minValueList.get(expIndex) > value)
					{
						minValueList.set(expIndex, value);
					}
				}
			}
		}

		Grouping g = ced.getGrouping();

		List<Integer> firstInd = new ArrayList<Integer>();
		List<Integer> seconInd = new ArrayList<Integer>();

		if (g != null)
		{
			for (int n : new ArrayList<Integer>(g.getGroup1()))
			{
				firstInd.add(expNoToIndex.get(n));
			}
			for (int n : new ArrayList<Integer>(g.getGroup2()))
			{
				seconInd.add(expNoToIndex.get(n));
			}
		}

		if (!experimentMapsList.isEmpty() && firstInd.isEmpty())
		{
			firstInd.add(0);
		}

		setDataToBeUsed(firstInd, seconInd);
	}

	/**
	 * Checks if any microarray data is loaded.
	 * @return true if data is loaded
	 */
	public boolean isDataAvailable()
	{
		return this.experimentMapsList != null;
	}

	/**
	 * @return , the loaded microarray data
	 */
	public ChisioExperimentData getCed()
	{
		return ced;
	}

	/**
	 * Discards the previously loaded microarray data.
	 */
	public void discardMicroarrayData()
	{
		this.ced = null;
		this.experimentMapsList = null;
		this.experimentInfoList = null;
		this.experimentNameList = null;
		this.firstExpIndices.clear();
		this.secondExpIndices.clear();
		System.gc();
	}

	/**
	 * When the user visualizes one experiment, this method is used to specify
	 * this experiment.
	 * @param index of the experiment
	 */
	public void setDataToBeUsed(List<Integer> index)
	{
		this.setDataToBeUsed(index, null);
	}

	/**
	 * When the user compares values in two experiments, this method is used.
	 * @param index1
	 * @param index2
	 */
	public void setDataToBeUsed(List<Integer> index1, List<Integer> index2)
	{
		firstExpIndices.clear();
		secondExpIndices.clear();

		firstExpIndices.addAll(index1);

		if (index2 != null)
		{
			for (int i : secondExpIndices)
			{
				assert !firstExpIndices.contains(i) :
					"Experiment indices overlaps! index: " + i;
			}

			secondExpIndices.addAll(index2);
		}

		if (type.equals(ExperimentData.EXPRESSION_DATA))
		{
			ExpressionData.maxValue = this.getMaxValue();
			ExpressionData.minValue = this.getMinValue();
		}
		else if (type.equals(ExperimentData.MASS_SPEC_DATA))
		{
			MassSpecData.maxValue = this.getMaxValue();
			MassSpecData.minValue = this.getMinValue();
		}
		else if (type.equals(ExperimentData.COPY_NUMBER_VARIATION))
		{
			CopyNumberData.maxValue = this.getMaxValue();
			CopyNumberData.minValue = this.getMinValue();
		}
		else if (type.equals(ExperimentData.MUTATION_DATA))
		{
			// Nothing to do here
		}

		Grouping g = ced.getGrouping();
		if (g == null)
		{
			try
			{
				g = (new ObjectFactory()).createGrouping();
				ced.setGrouping(g);
			}
			catch(Exception e) {e.printStackTrace();}
		}
		List g1 = g.getGroup1();
		g1.clear();
		for (int i : firstExpIndices)
		{
			g1.add(((Experiment) ced.getExperiment().get(i)).getNo());
		}
		List g2 = g.getGroup2();
		g2.clear();
		for (int i : secondExpIndices)
		{
			g2.add(((Experiment) ced.getExperiment().get(i)).getNo());
		}
	}

	public List<Integer> getFirstExpIndices()
	{
		return firstExpIndices;
	}

	public List<Integer> getSecondExpIndices()
	{
		return secondExpIndices;
	}

	/**
	 * Checks if each experiment has a separate info.
	 */
	public boolean isExpInfoAvailable()
	{
		return this.experimentMapsList != null;
	}

	/**
	 * Checks if an overall info for experiments is available.
	 */
	public boolean isDatasetInfoAvailable()
	{
		return this.datasetInfo != null;
	}

	/**
	 * Gets the dataset info of the loaded experiments.
	 */
	public String getDatasetInfo()
	{
		return datasetInfo;
	}

	/**
	 * Gets the info of the experiment specified with the parameter index.
	 * @param index
	 */
	public String getExperimentInfo(int index)
	{
		return this.experimentInfoList.get(index);
	}

	/**
	 * Gets the name of the experiment specified with the parameter index.
	 * @param index
	 */
	public String getExperimentName(int index)
	{
		return this.experimentNameList.get(index);
	}

	public int getAveraging()
	{
		return averaging;
	}

	public void setAveraging(int averaging)
	{
		assert averaging == MAX || averaging == MEAN || averaging == MEDIAN :
			"unknown averaging method : " + averaging;

		this.averaging = averaging;
	}

	public boolean isInCompareMode()
	{
		return !getSecondExpIndices().isEmpty();
	}

	/**
	 * Gets the maximum expression value in the current experiment(s).
	 * @return max value
	 */
	public double getMaxValue()
	{
		if (!firstExpIndices.isEmpty())
		{
			double firstMax = -Double.MAX_VALUE;

			for (int i : firstExpIndices)
			{
				double first = maxValueList.get(i);
				if (first > firstMax) firstMax = first;
			}

			if (secondExpIndices.isEmpty())
			{
				return firstMax;
			}
			else
			{
				double secondMax = -Double.MAX_VALUE;

				for (int i : secondExpIndices)
				{
					double second = maxValueList.get(i);
					if (second > secondMax) secondMax = second;
				}

				return Math.max(firstMax, secondMax);
			}
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Gets the minimum expression value in the current experiment(s).
	 * @return min value
	 */
	public double getMinValue()
	{
		if (!firstExpIndices.isEmpty())
		{
			double firstMin = Double.MAX_VALUE;

			for (int i : firstExpIndices)
			{
				double first = minValueList.get(i);
				if (first < firstMin) firstMin = first;
			}

			if (secondExpIndices.isEmpty())
			{
				return firstMin;
			}
			else
			{
				double secondMin = Double.MAX_VALUE;

				for (int i : secondExpIndices)
				{
					double second = minValueList.get(i);
					if (second < secondMin) secondMin = second;
				}

				return Math.min(firstMin, secondMin);
			}
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Gets the number of loaded experiments in the dataset.
	 * @return number of loaded experiments
	 */
	public int getExperimentSize()
	{
		if (this.isDataAvailable())
		{
			return this.experimentMapsList.size();
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Gets the experiment value for the specified pid.
	 * @param ref Reference object to associate an experiment data
	 * @return related experiment data
	 */
	public ExperimentData getExperimentData(XRef ref)
	{
		if (firstExpIndices.isEmpty())
		{
			return null;
		}

		Double v1 = getValue(firstExpIndices, ref);

		if (v1 != null)
		{
			if (secondExpIndices.isEmpty())
			{
				return createExperimentData(v1);
			}

			Double v2 = getValue(secondExpIndices, ref);

			if (v2 != null)
			{
				return createxpErimentData(v1, v2);
			}
		}
		return null;
	}

	/**
	 * Associates experiment data to the related nodes of the parameter graphs.
	 * @param graphs graphs to iterate its nodes
	 */
	public void associateExperimentData(Collection<? extends Graph> graphs)
	{
		for (Graph graph : graphs)
		{
			associateExperimentData(graph);
		}
	}

	/**
	 * Associates experiment data to the related nodes of the parameter graph.
	 * @param graph graph to iterate its nodes
	 */
	public void associateExperimentData(Graph graph)
	{
		for (Node node : graph.getNodes())
		{
			associateExperimentData(node);
		}
	}

	/**
	 * Associates experiment data to the related nodes in the given list.
	 * @param nodes list of nodes to associate experiment data
	 */
	public void associateExperimentData(List<Node> nodes)
	{
		for (Node node : nodes)
		{
			associateExperimentData(node);
		}
	}

	/**
	 * Associates experiment data to the parameter patika obejct.
	 * @param node to associate experiment data
	 */
	public void associateExperimentData(Node node)
	{
		Set<ExperimentData> datas = new HashSet<ExperimentData>();

		for (XRef ref : node.getReferences())
		{
			ExperimentData data = this.getExperimentData(ref);

			if (data != null)
			{
				datas.add(data);
			}
		}

		if (!datas.isEmpty())
		{
			ExperimentData data = averageData(datas);
			node.setExperimentData(data);
		}
	}

	private ExperimentData averageData(Set<ExperimentData> dataList)
	{
		List<Double> v1s = new ArrayList<Double>();
		List<Double> v2s = new ArrayList<Double>();

		for (ExperimentData data : dataList)
		{
			v1s.add(data.getValue1());

			if (data.isDouble())
			{
				v2s.add(data.getValue2());
			}
		}

		assert v2s.isEmpty() || v2s.size() == v1s.size() :
			"Disaster! Mixed single and double experiments";

		if (v2s.isEmpty())
		{
			return createExperimentData(average(v1s));
		}
		else
		{
			return createxpErimentData(average(v1s), average(v2s));
		}
	}

	/**
	 * Clears all microarray related custom data from the nodes of the parameter
	 * patika graph.
	 *
	 * @param graph graph to clear data from nodes
	 */
	public void clearExperimentData(Graph graph)
	{
		for (Node node : graph.getNodes())
		{
			if (node.hasLabel(type))
			{
				node.removeLabel(type);
			}
		}
	}

	/**
	 * Gets the corresponding average of values mapped to the parameter PID in
	 * the specified experiment indices.
	 * @param expIndices experiments to use in calculating the value
	 * @param ref reference of the node to match with rows
	 * @return value calculated microarray value
	 */
	private Double getValue(List<Integer> expIndices, XRef ref)
	{
		if (expIndices.size() == 1) return getValue(expIndices.get(0), ref);

		List<Double> vals = new ArrayList<Double>();

		for (int i : expIndices)
		{
			Double v = getValue(i, ref);
			if (v != null) vals.add(v);
		}

		if (vals.isEmpty())
		{
			return null;
		}

		return average(vals);
	}

	private double average(Collection<Double> vals)
	{
		switch(this.averaging)
		{
			case MAX:
				return max(vals);
			case MEAN:
				return mean(vals);
			case MEDIAN:
				return median(vals);
			default:
				throw new RuntimeException(
					"Invalid averaging method: " + averaging);
		}
	}

	/**
	 * Gets the corresponding average of values mapped to the parameter PID in
	 * the specified experiment index.
	 * @param expIndex index of experiment to use
	 * @param ref reference to look for
	 * @return value value associated with ref in the experiment
	 */
	private Double getValue(int expIndex, XRef ref)
	{
		Map<XRef, List<Double>> map = this.experimentMapsList.get(expIndex);

		if (map.containsKey(ref))
		{
			switch(this.averaging)
			{
				case MAX:
					return max(map.get(ref));
				case MEAN:
					return mean(map.get(ref));
				case MEDIAN:
					return median(map.get(ref));
				default:
					throw new RuntimeException(
						"Invalid averaging method: " + averaging);
			}
		}
		else
		{
			return null;
		}
	}

	public Set<XRef> getReferenceSet()
	{
		if (firstExpIndices.isEmpty())
		{
			return null;
		}

		Set<XRef> set = new HashSet<XRef>();

		for (int i : firstExpIndices)
		{
			set.addAll(experimentMapsList.get(i).keySet());
		}

		return set;
	}

	private double max(Collection<Double> col)
	{
		double m = 0;

		for (double d : col)
		{
			if (Math.abs(d) > Math.abs(m))
			{
				m = d;
			}
		}
		return m;
	}

	private double mean(Collection<Double> col)
	{
		double sum = 0;
		for (double d : col)
		{
			sum += d;
		}
		return sum / col.size();
	}

	private double median(Collection<Double> col)
	{
		assert !col.isEmpty();

		Set set = new TreeSet<Double>(col);
		Object[] arr = set.toArray();
		if (col.size() % 2 == 0)
		{
			return ((Double)arr[col.size() / 2] +
				(Double)arr[(col.size() / 2) - 1]) / 2;
		}
		else
		{
			return (Double)arr[col.size() / 2];
		}
	}

	private ExperimentData createExperimentData(double v)
	{
		if (type.equals(ExperimentData.EXPRESSION_DATA))
		{
			return new ExpressionData(v);
		}
		else if (type.equals(ExperimentData.MASS_SPEC_DATA))
		{
			return new MassSpecData(v);
		}
		else if (type.equals(ExperimentData.COPY_NUMBER_VARIATION))
		{
			return new CopyNumberData(v);
		}
		else if (type.equals(ExperimentData.MUTATION_DATA))
		{
			return new MutationData(v);
		}
		else
		{
			throw new RuntimeException("Invalid data type: " + type);
		}
	}

	private ExperimentData createxpErimentData(double v1, double v2)
	{
		if (type.equals(ExperimentData.EXPRESSION_DATA))
		{
			return new ExpressionData(v1, v2);
		}
		else if (type.equals(ExperimentData.MASS_SPEC_DATA))
		{
			return new MassSpecData(v1, v2);
		}
		else if (type.equals(ExperimentData.COPY_NUMBER_VARIATION))
		{
			return new CopyNumberData(v1, v2);
		}
		else if (type.equals(ExperimentData.MUTATION_DATA))
		{
			return new MutationData(v1, v2);
		}
		else
		{
			throw new RuntimeException("Invalid data type: " + type);
		}
	}

	/**
	 * Saves the loaded experiment data to its original file location. This is useful for saving the
	 * grouping information which can be edited by user.
	 */
	public void saveData()
	{
		if (ced == null || fileLocation == null) return;

		try
		{
			JAXBContext jc = JAXBContext.newInstance("org.patika.mada.dataXML");
			Marshaller m = jc.createMarshaller();
			m.setProperty("jaxb.formatted.output", Boolean.TRUE);

			BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
			m.marshal(this.ced, writer);
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------
	// Section: Class constants
	//--------------------------------------------------------------------------

	// Constants for possible averaging methods

	public static final int MEAN = 0;
	public static final int MEDIAN = 1;
	public static final int MAX = 2;

}
