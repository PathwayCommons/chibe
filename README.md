# ChiBE: Chisio BioPAX Editor #

Chisio BioPAX Editor, or simply ChiBE, is a free editing and visualization tool for pathway models represented by the [BioPAX](http://www.biopax.org) format, using [SBGN](http://sbgn.org) Process Description Language, based on [Chisio](http://www.cs.bilkent.edu.tr/~ivis/chisio.html).

The tool features user-friendly display, viewing, and editing of pathway models. Pathway views are rendered in a feature-rich format, and may be laid out and edited with state-of-the-art layout and visualization technologies. In addition, facilities for querying a set of integrated pathways in [Pathway Commons](http://www.pathwaycommons.org) are provided. Furthermore, visualization of experimental data, including cancer genomics data of the [cBioPortal](http://cbioportal.org), overlaid on pathway views is supported in ChiBE. These capabilities are organized around a genomics-oriented workflow and offer a unique comprehensive pathway analysis solution for genomics researchers.

ChiBE is a Java application running on various platforms including Windows XP/Vista/7, Linux, and Mac OS X. It was built using [Chisio 2.0](http://www.cs.bilkent.edu.tr/~ivis/chisio.html) and [Eclipse GEF 3.1](http://www.eclipse.org/gef) for graph visualization, [Paxtools](http://www.biopax.org/paxtools) for accessing and manipulating BioPAX files, and [PATIKAmad](http://www3.interscience.wiley.com/journal/119815826/issue) for experiment data analysis.

### [ChiBE 2.2 User's guide](http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-2.2.UG.pdf)
<!--[ChiBE 2.1 User's guide](http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-2.1.UG.pdf)-->

For a quick guide on how to explore pathways in Pathway Commons, please see [this wiki page](https://github.com/PathwayCommons/chibe/wiki/HowToExplorePathways).

## Download ##

ChiBE is distributed under [Eclipse Public License](http://www.eclipse.org/org/documents/epl-v10.php).

Below are latest builds of ChiBE.

  * [Latest build for Mac](https://raw.githubusercontent.com/PathwayAndDataAnalysis/repo/master/chibe-builds/chibe-latest-build-macosx-x86.zip)
  * [Latest build for Windows](https://raw.githubusercontent.com/PathwayAndDataAnalysis/repo/master/chibe-builds/chibe-latest-build-win64.zip)
  * [Latest build for Linux](https://raw.githubusercontent.com/PathwayAndDataAnalysis/repo/master/chibe-builds/chibe-latest-build-linux.tar.gz)

Below are the latest releases of ChiBE 2.2 for different platforms.

  * Setup file for ChiBE 2.2 for Windows XP/Vista/7: [32-bit](http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.2.0-setup-win32-win32-x86.msi) - [64-bit](http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.2.0-setup-win32-win32-x86_64.msi)
  * ChiBE 2.2 distribution for Mac OS X (see "README.TXT" for instructions): [32-bit](http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.2.0-setup-macosx-x86.zip)
  * ChiBE 2.2 distribution for Linux (see "README.TXT" for instructions): [32-bit](http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.2.0-setup-gtk-linux-x86.zip) - [64-bit](http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.2.0-setup-gtk-linux-x86_64.zip)

<!--
* Setup file for ChiBE 2.1 for Windows XP/Vista/7: [http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.1.4-setup-win32-win32-x86.msi 32-bit] - [http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.1.4-setup-win32-win32-x86_64.msi 64-bit]
* ChiBE 2.1 distribution for Mac OS X (see "README.TXT" for instructions): [http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.1.4-setup-macosx-x86.zip 32-bit]
* ChiBE 2.1 distribution for Linux (see "README.TXT" for instructions): [http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.1.4-setup-gtk-linux-x86.zip 32-bit] - [http://www.cs.bilkent.edu.tr/~ivis/chibe/chibe-2.1.4-setup-gtk-linux-x86_64.zip 64-bit]

Note: ChiBE cannot run on Mac OS X with Java 1.7. This is because Apple decided to remove 32-bit support from Java 1.7 and after. We recommend using Java 1.6 with Mac OS X. Read [[MacAndJava6]] for how to do it.
-->

Please contact us if you need a build for another platform.

<!--
Here is the [http://resources.chibe.googlecode.com/hg/chibe-latest-build-macosx-x86.zip latest build for Mac] and  (beware: this is not a release).
-->

Please see the [[SetupGuide]] for running ChiBE from sources, and [[QuickStartGuide]] for fast examples.


## Highlights ##

Following are sample screenshots showing tool highlights.

<table cellspacing='4' border='1'>
<tr>
<td><a href='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-ss1.png'><img src='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-ss1-small.png' /></a></td>
<td><a href='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-ss2.png'><img src='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-ss2-small.png' /></a></td>
</tr>
</table>

Click on the figure to see the entire screenshot.

## Sample Pathways ##

Following are images of pathway models produced by ChiBE taken from some prominent databases.

<table cellspacing='4' border='1'>
<tr>
<td><a href='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-reactome.png'><img src='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-reactome-small.png' /></a>
</td>
<td>
<a href='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-nci.png'><img src='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-nci-small.png' /></a></td>
<td>
<a href='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-cancercellmap.png'><img src='http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-cancercellmap-small.png' /></a></td>
</tr>
</table>

Click on the figure to see the entire screenshot.

**(left)** [ERK/MAPK targets](http://www.reactome.org/cgi-bin/eventbrowser?DB=gk_current&ID=198753) from Reactome **(middle)** [Class IB PI3K non-lipid kinase events](http://pid.nci.nih.gov/search/pathway_landing.shtml?pathway_id=pi3kcibpathway&pathway_name=Class%20IB%20PI3K%20non-lipid%20kinase%20events&source=NCI-Nature%20curated&what=graphic&jpg=on&ppage=1) from NCI PID **(right)** [Hedgehog signaling pathway](http://cancer.cellmap.org/cellmap/record.do?id=2209) from The Cancer Cell Map

### Release Notes for Version 2.2 (June 24, 2014) ###

  * Made architectural improvements in the core.
  * Added a Data Legend for the color-coding of the currently loaded experiment data.
  * Implemented an enriched-reactions query to rank reactions with a statistical measure.

### Release Notes for Version 2.1 (June 26, 2013) ###

  * Upgraded Pathway Commons interface to the latest version.
  * Added support for showing/hiding compartments.
  * Revised binary interaction types for SIF views.

### Release Notes for Version 2.0 (January 9, 2013) ###

  * BioPAX level 3 Support: Support for BioPAX level 3 models as well as level 2 models is now provided seamlessly in ChiBE.
  * SBGN Process Description Language Compliance: Loaded pathway models are now rendered using a language mostly compliant with SBGN's Process Description Language.
  * Remote Querying: Now ChiBE can send graph queries to Pathway Commons database.
  * Local Querying: Loaded pathway models can now be queried to search for local relationships.
  * Fetching From GEO: ChiBE now offers an alternative way for visualizing expression data on pathways, if an expression data stored in NCBI GEO is to be visualized on a pathway from Pathway Commons. With providing the GEO accession number of series file only, user will be able to visualize the particular expression set in ChiBE.
  * Fetching From cBio Portal: cBio Cancer Genomics Portal is a cancer genomics analysis portal, which offers the chance to explore cancer genomes with respect to mutations, copy number alterations, mRNA expression changes, DNA methylation values, and protein levels. ChiBE enables users analysis of such data at pathway level. It collects data from cBio Portal and integrates with the model to display the alterations associated with each entity.
  * Upgraded Chisio, ChiBE rendering and layout engine, to version 2.0.

### Release Notes for Version 1.1 (July 10, 2009) ###

  * Support for Mac OS X
  * Improved persistence of pathway views as image
  * Several minor bug fixes

## Team ##
  * Ozgun Babur, Emek Demir, B.Arman Aksoy, Nikolaus Schultz, Chris Sander, [cbio at MSKCC](http://cbio.mskcc.org)
  * Ugur Dogrusoz, Merve Cakir, [i-Vis at Bilkent University](http://www.cs.bilkent.edu.tr/~ivis)

ChiBE's requirements were determined by OB, UD, ED, NS, and CS. The software design and development were mainly performed by OB, with help from MC, AA, and UD.

### Acknowledgements ###
We would like to thank IntelliJ for providing a free open source license for IDEA.

[![IntelliJ IDEA](http://imagej.net/_images/thumb/1/1b/Intellij-idea.png/97px-Intellij-idea.png)](http://www.jetbrains.com/idea "IntelliJ IDEA")

### Publications ###

Babur, Ö., Dogrusoz, U., Çakir, M., Aksoy, B. A., Schultz, N., Sander, C., & Demir, E. (2014). [Integrating biological pathways and genomic profiles with ChiBE 2](http://www.biomedcentral.com/1471-2164/15/642). BMC genomics, 15(1), 642.

Babur, O., Dogrusoz, U., Demir, E., & Sander, C. (2010). [ChiBE: interactive visualization and manipulation of BioPAX pathway models](http://bioinformatics.oxfordjournals.org/content/26/3/429.long). Bioinformatics, 26(3), 429-431.
