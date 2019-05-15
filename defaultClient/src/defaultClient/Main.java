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
package defaultClient;



import cartago.CartagoService;
import cartago.util.BasicLogger;

public class Main 
{
	
	public static void main(String[] args) throws Exception {
		String ip = "";
	    CartagoService.startNode();
	    CartagoService.installInfrastructureLayer("default");
	    boolean log = false;
	    if(args.length < 1)
	    {
	    	CartagoService.startInfrastructureService("default", "localhost:8080");
	    	ip = "localhost:8080";
	    }
	    else
	    {
	    	CartagoService.startInfrastructureService("default", args[0]);
	    	ip = args[0];
	    	if(args.length > 1)
	    		log = Boolean.parseBoolean(args[1]);
	    }
	    if(log)
	    	CartagoService.registerLogger("default",new BasicLogger());
	    //CartagoService.enableLinkingWithNode(id, support, address);
	    System.out.println("CArtAgO Node Ready on " + ip);	   	   	   
	  }

}
