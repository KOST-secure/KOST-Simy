/* == KOST-Simy =================================================================================
 * The KOST-Simy application is used for Compare Image-Files. Copyright (C) 2015 Claire
 * R�thlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * KOST-Simy is a development of the KOST-CECO. All rights rest with the KOST-CECO. This application
 * is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This application is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or
 * see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostsimy.comparison.moduleim;

import java.io.File;

import ch.kostceco.tools.kostsimy.comparison.ComparisonModule;
import ch.kostceco.tools.kostsimy.exception.moduleim.CompareImageException;

/** Vergleicht die beiden Bilder mit ImageMagick Compare und wertet das Resultat aus
 * 
 * @author Rc Claire R�thlisberger, KOST-CECO */

public interface CompareImageModule extends ComparisonModule
{

	public boolean validate( File origDatei, File repDatei, File directoryOfLogfile  )
			throws CompareImageException;

}