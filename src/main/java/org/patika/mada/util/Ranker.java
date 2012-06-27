package org.patika.mada.util;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Ranker<E>
{
	Map<E, Integer> cntMap;

	public Ranker()
	{
		cntMap = new HashMap<E, Integer>();
	}

	public void count(E obj)
	{
		if (!cntMap.containsKey(obj)) cntMap.put(obj, 0);
		cntMap.put(obj, cntMap.get(obj) + 1);
	}

	public List<List<E>> getRankedList()
	{
		List<List<E>> ranking = new ArrayList<List<E>>();

		if (cntMap.isEmpty()) return ranking;

		List<Holder> list = new ArrayList<Holder>();

		for (E e : cntMap.keySet())
		{
			list.add(new Holder(e, cntMap.get(e)));
		}

		Object[] holders = list.toArray();
		Arrays.sort(holders);

		int score = ((Holder) holders[holders.length - 1]).cnt + 1;
		List<E> group = null;

		for (int i = holders.length-1; i >= 0; i--)
		{
			Holder h = (Holder) holders[i];

			if (h.cnt < score)
			{
				score = h.cnt;
				group = new ArrayList<E>();
				ranking.add(group);
			}

			group.add(h.obj);
		}

		return ranking;
	}

	class Holder implements Comparable
	{
		E obj;
		Integer cnt;

		Holder(E obj, Integer cnt)
		{
			this.obj = obj;
			this.cnt = cnt;
		}

		public int compareTo(Object o)
		{
			if (o.getClass() == getClass())
			{
				Holder h = (Holder) o;
				return cnt.compareTo(h.cnt);
			}
			return 0;
		}
	}
}
