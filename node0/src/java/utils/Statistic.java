/*******************************************************************************
 *  ----------------------------------------------------------------------------------------------------------
 *  Copyright (C) 2015 Xavier Limón, Alejandro Guerra-Hernández, Nicandro Cruz-Ramírez, Francisco Grimaldo-Moreno
 * Departmento de Inteligencia Artificial. Universidad Veracruzana. 
 * Departament d’Informàtica. Universitat de València
 *
 *  This file is part of JaCa-DDM.
 *
 *     JaCa-DDM is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     JaCa-DDM is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JaCa-DDM.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  To contact the authors:
 *    xavier120@hotmail.com, aguerra@uv.mx, francisco.grimaldo@uv.es
 *    http://www.uv.mx/aguerra/
 *   http://www.uv.es/grimo/
 * -----------------------------------------------------------------------------------------------------------
 *******************************************************************************/
package utils;

public class Statistic {

	private static int decimal=3;
	
	public static double Mean (Object[] list){
		double sum= Sum(list);
		double size=list.length;
		return truncate(sum/size,decimal);		
	}
		
	public static double StdDev(Object[] list){
		double squareSum=0, num, stdDev;
		double mean= Mean(list);
		double size=list.length;
		for (Object elt: list)
		{    
			num=Double.parseDouble(elt.toString());
            squareSum += (num - mean) * (num - mean);	       
		}
		stdDev= Math.sqrt( squareSum/(size - 1) );
		return truncate(stdDev,decimal);
	}
		
	public static double Sum (Object[] list){
		if (list.length > 0) {
			double sum = 0;
			for (Object elt: list)
			{				
						double num=Double.parseDouble(elt.toString());
	                    sum += num;
			}	
			return sum;
		}		
		else        	
		return 0;
	}
	
	
	// truncate a value n places
	public static double truncate(double value, int places) {
	    double multiplier = Math.pow(10, places);
	    return Math.floor(multiplier * value) / multiplier;
	}	
}
