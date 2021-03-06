/* == KOST-Simy =================================================================================
 * The KOST-Simy application is used for Compare Image-Files. Copyright (C) 2015-2018 Claire
 * Röthlisberger (KOST-CECO)
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

package ch.kostceco.tools.kostsimy.comparison.moduleim.impl;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import ch.kostceco.tools.kostsimy.comparison.ComparisonModuleImpl;
import ch.kostceco.tools.kostsimy.comparison.moduleim.CompareImageModule;
import ch.kostceco.tools.kostsimy.exception.moduleim.CompareImageException;
import ch.kostceco.tools.kostsimy.service.ConfigurationService;
import ch.kostceco.tools.kostsimy.util.StreamGobbler;
import ch.kostceco.tools.kostsimy.util.Util;

/** Vergleicht die beiden Bilder mit ImageMagick Compare und wertet das Resultat aus
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class CompareImageModuleImpl extends ComparisonModuleImpl implements CompareImageModule
{
	public static String					NEWLINE	= System.getProperty( "line.separator" );
	private ConfigurationService	configurationService;

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File origDatei, File repDatei, File directoryOfLogfile,
			String imToleranceTxt ) throws CompareImageException
	{
		boolean isValid = true;
		// boolean isValidFailed = false;
		boolean compResult = false;
		int allInt = 0;
		boolean allNoInt = false;
		String allStr = "";

		String imTolerance = "5%";
		float percentageInvalid = 99.9999f;

		/* Initialisierung ImageMagick -> überprüfen der Angaben: existiert die compare.exe,
		 * msvcp120.dll, msvcr120.dll, vcomp120.dll am vorgegebenen Ort? */
		String compareExe = "compare.exe";
		String msvcp120Dll = "msvcp120.dll";
		String msvcr120Dll = "msvcr120.dll";
		String vcomp120Dll = "vcomp120.dll";
		String im = "resources" + File.separator + "ImageMagickCompare-6.9.1-Q16";
		boolean imExist = true;
		File fCompareExe = new File( im + File.separator + compareExe );
		if ( !fCompareExe.exists() ) {
			// Compare.exe von ImageMagick existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, compareExe ) );
			imExist = false;
		}
		File fMsvcp120Dll = new File( im + File.separator + msvcp120Dll );
		if ( !fMsvcp120Dll.exists() ) {
			// msvcp120.dll von ImageMagick existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, msvcp120Dll ) );
			imExist = false;
		}
		File fMsvcr120Dll = new File( im + File.separator + msvcr120Dll );
		if ( !fMsvcr120Dll.exists() ) {
			// msvcr120.dll von ImageMagick existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, msvcr120Dll ) );
			imExist = false;
		}
		File fVcomp120Dll = new File( im + File.separator + vcomp120Dll );
		if ( !fVcomp120Dll.exists() ) {
			// vcomp120.dll von ImageMagick existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, vcomp120Dll ) );
			imExist = false;
		}
		if ( !imExist ) {
			// compare.exe/msvcp120.dll/msvcr120.dll/vcomp120.dll von ImageMagick existiert nicht -->
			// Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, vcomp120Dll ) );
			return false;
		}

		if ( imToleranceTxt.contains( "N" ) || imToleranceTxt.contains( "n" ) ) {
			// null = 0%
			imTolerance = "0%";
			percentageInvalid = (float) 100.000000000;
			imToleranceTxt = "N";
		} else if ( imToleranceTxt.contains( "S" ) || imToleranceTxt.contains( "s" ) ) {
			// small = 2%
			imTolerance = "2%";
			percentageInvalid = (float) 99.9999;
			imToleranceTxt = "S";
		} else if ( imToleranceTxt.contains( "M" ) || imToleranceTxt.contains( "m" ) ) {
			// medium = 5%
			imTolerance = "5%";
			imToleranceTxt = "M";
			percentageInvalid = (float) 99.999;
		} else if ( imToleranceTxt.contains( "XL" ) || imToleranceTxt.contains( "xl" ) ) {
			// xlarge = 15%
			imTolerance = "15%";
			percentageInvalid = (float) 99.9;
			imToleranceTxt = "XL";
		} else if ( imToleranceTxt.contains( "L" ) || imToleranceTxt.contains( "l" ) ) {
			// large = 10%
			imTolerance = "10%";
			percentageInvalid = (float) 99.99;
			imToleranceTxt = "L";
		} else {
			// null = 0%
			imTolerance = "0%";
			percentageInvalid = (float) 100.000000000;
			imToleranceTxt = "N";
		}
		String pathToCompareExe = fCompareExe.getAbsolutePath();

		File report;
		File reportId;
		String imgPx1 = "1";

		StringBuffer concatenatedOutputs = new StringBuffer();

		try {

			String pathToOutput = directoryOfLogfile.getAbsolutePath() + File.separator
					+ origDatei.getName() + "_compare_report.txt";
			String pathToOutputId = directoryOfLogfile.getAbsolutePath() + File.separator
					+ origDatei.getName() + "_identify_report.txt";
			String pathToMask = directoryOfLogfile.getAbsolutePath() + File.separator
					+ origDatei.getName() + "_mask.jpg";

			/* compare -fuzz 15% -metric AE -quiet -identify -verbose -highlight-color DarkRed Image_1.jpg
			 * Image_2.jpg mask.jpg >>results_id.txt 2>results.txt */

			String command = "cmd /c \"\"" + pathToCompareExe + "\" -fuzz " + imTolerance
					+ " -metric AE -quiet -identify -verbose -highlight-color DarkRed \""
					+ origDatei.getAbsolutePath() + "\" \"" + repDatei.getAbsolutePath() + "\" \""
					+ pathToMask + "\" >>\"" + pathToOutputId + "\" 2>\"" + pathToOutput + "\"";
			/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
			 * gehts: cmd /c\"urspruenlicher Befehl\" */

			Process proc = null;
			Runtime rt = null;

			try {
				report = new File( pathToOutput );
				reportId = new File( pathToOutputId );
				File mask = new File( pathToMask );
				// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, löschen
				// wir es
				if ( report.exists() ) {
					report.delete();
				}
				if ( reportId.exists() ) {
					reportId.delete();
				}
				if ( mask.exists() ) {
					mask.delete();
				}
				Util.switchOffConsole();
				rt = Runtime.getRuntime();
				proc = rt.exec( command.toString().split( " " ) );
				// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

				// Fehleroutput holen
				StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

				// Output holen
				StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

				// Threads starten
				errorGobbler.start();
				outputGobbler.start();

				// Warte, bis wget fertig ist
				proc.waitFor();

				Util.switchOnConsole();
				// Kontrolle ob die Reports existieren
				if ( !report.exists() ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
											report.getAbsolutePath() ) );
					if ( !reportId.exists() ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
										+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
												reportId.getAbsolutePath() ) );
						return false;
					}
					return false;
				}
				if ( !reportId.exists() ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
											reportId.getAbsolutePath() ) );
					return false;
				}
			} catch ( Exception e ) {
				getMessageService()
						.logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
										+ getTextResourceService().getText( ERROR_XML_IMCMP_SERVICEFAILED,
												e.getMessage() ) );
				return false;
			} finally {
				if ( proc != null ) {
					closeQuietly( proc.getOutputStream() );
					closeQuietly( proc.getInputStream() );
					closeQuietly( proc.getErrorStream() );
				}
			}
			// Kontrolle ob die Reports existieren
			if ( !report.exists() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
								+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
										report.getAbsolutePath() ) );
				if ( !reportId.exists() ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
											reportId.getAbsolutePath() ) );
					return false;
				}
				return false;
			}
			if ( !reportId.exists() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
								+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
										reportId.getAbsolutePath() ) );
				return false;
			}

			// Ende IMCMP direkt auszulösen

			// TODO: Marker: ReportId und auswerten (Grösse und der Pixel)
			try {
				BufferedReader in = new BufferedReader( new FileReader( reportId ) );
				String line;
				String imgSize1 = "1";
				String imgSize2 = "2";
				String imgPx2 = "2";

				while ( (line = in.readLine()) != null ) {

					concatenatedOutputs.append( line );
					concatenatedOutputs.append( NEWLINE );

					/* Format: TIFF (Tagged Image File Format) Mime type: image/tiff
					 * 
					 * Geometry: 2469x3568+0+0
					 * 
					 * Channel statistics:
					 * 
					 * Pixels: 8809392
					 * 
					 * Geometry und Pixels scheinen immer ausgegeben zu werden
					 * 
					 * Gemotry und Pixels müssen identisch sein */
					if ( line.contains( "  Geometry: " ) ) {
						if ( imgSize1.equals( "1" ) ) {
							imgSize1 = line;
						} else {
							imgSize2 = line;
						}
					} else if ( line.contains( "  Pixels: " ) ) {
						if ( imgPx1.equals( "1" ) ) {
							imgPx1 = line;
						} else {
							imgPx2 = line;
						}
					}

					// TODO: Marker: Auswertung und Fehlerausgabe wenn nicht bestanden.
				}
				if ( imgPx1.equals( "1" ) && imgPx2.equals( "2" ) && imgSize1.equals( "1" )
						&& imgSize2.equals( "2" ) ) {
					// identify_report ist leer oder enthält nicht das was er sollte
					isValid = false;
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORTTEXT ) );
					in.close();
					return false;
				}
				if ( !imgPx1.equals( imgPx2 ) ) {
					// die beiden Bilder haben nicht gleich viel Pixels
					isValid = false;
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
									+ getTextResourceService().getText( ERROR_XML_CI_PIXELINVALID, imgPx1, imgPx2 ) );
				}
				if ( !imgSize1.equals( imgSize2 ) ) {
					// die beiden Bilder sind nicht gleich gross
					isValid = false;
					getMessageService()
							.logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
											+ getTextResourceService().getText( ERROR_XML_CI_SIZEINVALID, imgSize1,
													imgSize2 ) );
				}
				if ( !isValid ) {
					// die beiden Bilder sind nicht gleich gross
					in.close();
					return false;
				}
				in.close();
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
										"identify: " + e.getMessage() ) );
				return false;
			}

			// TODO: Marker: Report auswerten (Bildvergleich) wenn grösse & PixelAnzahl identisch
			try {
				BufferedReader in = new BufferedReader( new FileReader( report ) );
				String line;
				boolean allNull = false;
				boolean allExist = false;

				while ( (line = in.readLine()) != null ) {

					concatenatedOutputs.append( line );
					concatenatedOutputs.append( NEWLINE );

					/* img1.tif[0] TIFF 2469x3568 2469x3568+0+0 8-bit Grayscale Gray 8.833MB 1.451u 0:01.774
					 * 
					 * img2.jp2[0] JP2 2469x3568 2469x3568+0+0 8-bit Gray 1.842MB 1.357u 0:01.378
					 * 
					 * all: 0
					 * 
					 * in den ersten zwei zeilen sind die eigenschaften der beiden Bilder enthalten
					 * 
					 * Danach die Anzahl Pixel mit einer grösseren Abweichung aus, allg: 0= vergleichbar */
					if ( line.contains( " all: " ) ) {
						allExist = true;
						if ( line.contains( " all: 0" ) ) {
							allNull = true;
						} else {
							/* Invalide Px extrahieren "    all: 3563" extrahieren */
							String lineReportAll = line.substring( 9 );
							try {
								// lineReport = 3563
								allInt = Integer.parseInt( lineReportAll );
							} catch ( Exception e ) {
								allNoInt = true;
								allStr = lineReportAll;
							}
						}
					}

					if ( allNull ) {
						compResult = true;
					}
				}
				in.close();
				if ( !allExist ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOALL ) );
					return false;
				}
				if ( !compResult ) {
					// Bildvergleich nicht bestanden
					if ( allNoInt ) {
						/* Bilder mit vielen Pixels die Abweichen (Potenz -> String): Vereinfachte Fehlerausgabe */

						/* Invalide [allStr] und total px z2 aus imgPx1 "    Pixels: 8809392" extrahieren */
						String lineReport = imgPx1.substring( 12 );
						// lineReport = 8809392

						isValid = false;

						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
										+ getTextResourceService().getText( ERROR_XML_CI_CIINVALIDSTR, lineReport,
												imToleranceTxt, allStr ) );

					} else {
						/* Bilder mit einer Abweichung (Int): Prozent ermitteln und mit percentageInvalid
						 * abgleichen */
						double z1 = 0;
						double z2 = 0;
						float percentageCalc = (float) 0.0;
						float percentageCalcInv = (float) 0.0;

						/* Invalide z1 [allInt] und total px z2 aus imgPx1 "    Pixels: 8809392" extrahieren */
						String lineReport = imgPx1.substring( 12 );
						// lineReport = 8809392
						z2 = Double.parseDouble( lineReport );
						z1 = allInt;

						percentageCalc = (float) (100 - (100 / z2 * z1));
						percentageCalcInv = 100 - percentageCalc;

						// Prozentzahlen vergleichen
						if ( percentageInvalid > percentageCalc ) {
							// Bilder mit einer grösseren Abweichung
							isValid = false;

							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
											+ getTextResourceService().getText( ERROR_XML_CI_CIINVALID,
													percentageCalcInv, z2, imToleranceTxt, z1 ) );
						}
					}
				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
								+ getTextResourceService()
										.getText( ERROR_XML_UNKNOWN, "compare: " + e.getMessage() ) );
				return false;
			}
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// reports löschen
		if ( report.exists() ) {
			report.delete();
		}
		if ( reportId.exists() ) {
			reportId.delete();
		}

		return isValid;
	}
}
