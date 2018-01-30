package org.gvt.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A merge of Cosmic Cancer Census and OncoKB.
 *
 */
public class CancerGenes
{
	public static boolean isCancerGene(String gene)
	{
		return genes.contains(gene);
	}

	private static final Set<String> genes = new HashSet<String>(Arrays.asList(
		("A1CF\n" +
			"ABI1\n" +
			"ABL1\n" +
			"ABL2\n" +
			"ACKR3\n" +
			"ACSL3\n" +
			"ACSL6\n" +
			"ACVR1\n" +
			"ACVR2A\n" +
			"AFF1\n" +
			"AFF3\n" +
			"AFF4\n" +
			"AKAP9\n" +
			"AKT1\n" +
			"AKT2\n" +
			"AKT3\n" +
			"ALDH2\n" +
			"ALK\n" +
			"AMER1\n" +
			"ANK1\n" +
			"APC\n" +
			"APOBEC3B\n" +
			"AR\n" +
			"ARAF\n" +
			"ARHGAP26\n" +
			"ARHGAP5\n" +
			"ARHGEF12\n" +
			"ARID1A\n" +
			"ARID1B\n" +
			"ARID2\n" +
			"ARNT\n" +
			"ASPSCR1\n" +
			"ASXL1\n" +
			"ASXL2\n" +
			"ATF1\n" +
			"ATIC\n" +
			"ATM\n" +
			"ATP1A1\n" +
			"ATP2B3\n" +
			"ATR\n" +
			"ATRX\n" +
			"AXIN1\n" +
			"AXIN2\n" +
			"B2M\n" +
			"BAP1\n" +
			"BARD1\n" +
			"BCL10\n" +
			"BCL11A\n" +
			"BCL11B\n" +
			"BCL2\n" +
			"BCL2L12\n" +
			"BCL3\n" +
			"BCL6\n" +
			"BCL7A\n" +
			"BCL9\n" +
			"BCL9L\n" +
			"BCLAF1\n" +
			"BCOR\n" +
			"BCORL1\n" +
			"BCR\n" +
			"BIRC3\n" +
			"BIRC6\n" +
			"BLM\n" +
			"BMP5\n" +
			"BMPR1A\n" +
			"BRAF\n" +
			"BRCA1\n" +
			"BRCA2\n" +
			"BRD3\n" +
			"BRD4\n" +
			"BRIP1\n" +
			"BTG1\n" +
			"BTK\n" +
			"BUB1B\n" +
			"C15orf65\n" +
			"C2orf44\n" +
			"CACNA1D\n" +
			"CALR\n" +
			"CAMTA1\n" +
			"CANT1\n" +
			"CARD11\n" +
			"CARS\n" +
			"CASC5\n" +
			"CASP8\n" +
			"CBFA2T3\n" +
			"CBFB\n" +
			"CBL\n" +
			"CBLB\n" +
			"CBLC\n" +
			"CCDC6\n" +
			"CCNB1IP1\n" +
			"CCNC\n" +
			"CCND1\n" +
			"CCND2\n" +
			"CCND3\n" +
			"CCNE1\n" +
			"CCR4\n" +
			"CCR7\n" +
			"CD209\n" +
			"CD274\n" +
			"CD28\n" +
			"CD74\n" +
			"CD79A\n" +
			"CD79B\n" +
			"CDC73\n" +
			"CDH1\n" +
			"CDH10\n" +
			"CDH11\n" +
			"CDH17\n" +
			"CDK12\n" +
			"CDK4\n" +
			"CDK6\n" +
			"CDKN1A\n" +
			"CDKN1B\n" +
			"CDKN2A\n" +
			"CDKN2C\n" +
			"CDX2\n" +
			"CEBPA\n" +
			"CEP89\n" +
			"CHCHD7\n" +
			"CHD2\n" +
			"CHD4\n" +
			"CHEK2\n" +
			"CHIC2\n" +
			"CHST11\n" +
			"CIC\n" +
			"CIITA\n" +
			"CLIP1\n" +
			"CLP1\n" +
			"CLTC\n" +
			"CLTCL1\n" +
			"CNBD1\n" +
			"CNBP\n" +
			"CNOT3\n" +
			"CNTNAP2\n" +
			"CNTRL\n" +
			"COL1A1\n" +
			"COL2A1\n" +
			"COL3A1\n" +
			"COX6C\n" +
			"CREB1\n" +
			"CREB3L1\n" +
			"CREB3L2\n" +
			"CREBBP\n" +
			"CRLF2\n" +
			"CRNKL1\n" +
			"CRTC1\n" +
			"CRTC3\n" +
			"CSF1R\n" +
			"CSF3R\n" +
			"CSMD3\n" +
			"CTCF\n" +
			"CTNNA2\n" +
			"CTNNB1\n" +
			"CTNND1\n" +
			"CTNND2\n" +
			"CUL3\n" +
			"CUX1\n" +
			"CXCR4\n" +
			"CYLD\n" +
			"CYP2C8\n" +
			"CYSLTR2\n" +
			"DAXX\n" +
			"DCAF12L2\n" +
			"DCC\n" +
			"DCTN1\n" +
			"DDB2\n" +
			"DDIT3\n" +
			"DDR2\n" +
			"DDX10\n" +
			"DDX3X\n" +
			"DDX5\n" +
			"DDX6\n" +
			"DEK\n" +
			"DGCR8\n" +
			"DICER1\n" +
			"DNAJB1\n" +
			"DNM2\n" +
			"DNMT3A\n" +
			"DROSHA\n" +
			"DUX4L1\n" +
			"EBF1\n" +
			"ECT2L\n" +
			"EED\n" +
			"EGFR\n" +
			"EIF1AX\n" +
			"EIF3E\n" +
			"EIF4A2\n" +
			"ELF3\n" +
			"ELF4\n" +
			"ELK4\n" +
			"ELL\n" +
			"ELN\n" +
			"EML4\n" +
			"EP300\n" +
			"EPAS1\n" +
			"EPHA3\n" +
			"EPHA7\n" +
			"EPS15\n" +
			"ERBB2\n" +
			"ERBB3\n" +
			"ERBB4\n" +
			"ERC1\n" +
			"ERCC2\n" +
			"ERCC3\n" +
			"ERCC4\n" +
			"ERCC5\n" +
			"ERG\n" +
			"ESR1\n" +
			"ETNK1\n" +
			"ETV1\n" +
			"ETV4\n" +
			"ETV5\n" +
			"ETV6\n" +
			"EWSR1\n" +
			"EXT1\n" +
			"EXT2\n" +
			"EZH2\n" +
			"EZR\n" +
			"FAM131B\n" +
			"FAM135B\n" +
			"FAM46C\n" +
			"FAM47C\n" +
			"FANCA\n" +
			"FANCC\n" +
			"FANCD2\n" +
			"FANCE\n" +
			"FANCF\n" +
			"FANCG\n" +
			"FAS\n" +
			"FAT1\n" +
			"FAT3\n" +
			"FAT4\n" +
			"FBLN2\n" +
			"FBXO11\n" +
			"FBXW7\n" +
			"FCGR2B\n" +
			"FCRL4\n" +
			"FEN1\n" +
			"FES\n" +
			"FEV\n" +
			"FGFR1\n" +
			"FGFR1OP\n" +
			"FGFR2\n" +
			"FGFR3\n" +
			"FGFR4\n" +
			"FH\n" +
			"FHIT\n" +
			"FIP1L1\n" +
			"FKBP9\n" +
			"FLCN\n" +
			"FLI1\n" +
			"FLNA\n" +
			"FLT3\n" +
			"FLT4\n" +
			"FNBP1\n" +
			"FOXA1\n" +
			"FOXL2\n" +
			"FOXO1\n" +
			"FOXO3\n" +
			"FOXO4\n" +
			"FOXP1\n" +
			"FOXR1\n" +
			"FSTL3\n" +
			"FUBP1\n" +
			"FUS\n" +
			"GAS7\n" +
			"GATA1\n" +
			"GATA2\n" +
			"GATA3\n" +
			"GLI1\n" +
			"GMPS\n" +
			"GNA11\n" +
			"GNAQ\n" +
			"GNAS\n" +
			"GOLGA5\n" +
			"GOPC\n" +
			"GPC3\n" +
			"GPHN\n" +
			"GRIN2A\n" +
			"GRM3\n" +
			"H3F3A\n" +
			"H3F3B\n" +
			"HERPUD1\n" +
			"HEY1\n" +
			"HIF1A\n" +
			"HIP1\n" +
			"HIST1H3B\n" +
			"HIST1H4I\n" +
			"HLA-A\n" +
			"HLF\n" +
			"HMGA1\n" +
			"HMGA2\n" +
			"HMGN2P46\n" +
			"HNF1A\n" +
			"HNRNPA2B1\n" +
			"HOOK3\n" +
			"HOXA11\n" +
			"HOXA13\n" +
			"HOXA9\n" +
			"HOXC11\n" +
			"HOXC13\n" +
			"HOXD11\n" +
			"HOXD13\n" +
			"HRAS\n" +
			"HSP90AA1\n" +
			"HSP90AB1\n" +
			"ID3\n" +
			"IDH1\n" +
			"IDH2\n" +
			"IGH\n" +
			"IGK\n" +
			"IGL\n" +
			"IKBKB\n" +
			"IKZF1\n" +
			"IL2\n" +
			"IL21R\n" +
			"IL6ST\n" +
			"IL7R\n" +
			"IRF4\n" +
			"IRS4\n" +
			"ISX\n" +
			"ITGAV\n" +
			"ITK\n" +
			"JAK1\n" +
			"JAK2\n" +
			"JAK3\n" +
			"JAZF1\n" +
			"JUN\n" +
			"KAT6A\n" +
			"KAT6B\n" +
			"KAT7\n" +
			"KCNJ5\n" +
			"KDM5A\n" +
			"KDM5C\n" +
			"KDM6A\n" +
			"KDR\n" +
			"KDSR\n" +
			"KEAP1\n" +
			"KIAA1549\n" +
			"KIAA1598\n" +
			"KIF5B\n" +
			"KIT\n" +
			"KLF4\n" +
			"KLF6\n" +
			"KLK2\n" +
			"KMT2A\n" +
			"KMT2C\n" +
			"KMT2D\n" +
			"KNSTRN\n" +
			"KRAS\n" +
			"KTN1\n" +
			"LARP4B\n" +
			"LASP1\n" +
			"LCK\n" +
			"LCP1\n" +
			"LEF1\n" +
			"LHFP\n" +
			"LIFR\n" +
			"LMNA\n" +
			"LMO1\n" +
			"LMO2\n" +
			"LPP\n" +
			"LRIG3\n" +
			"LRP1B\n" +
			"LSM14A\n" +
			"LYL1\n" +
			"LZTR1\n" +
			"MAF\n" +
			"MAFB\n" +
			"MALAT1\n" +
			"MALT1\n" +
			"MAML2\n" +
			"MAP2K1\n" +
			"MAP2K2\n" +
			"MAP2K4\n" +
			"MAP3K1\n" +
			"MAP3K13\n" +
			"MAPK1\n" +
			"MAX\n" +
			"MB21D2\n" +
			"MDM2\n" +
			"MDM4\n" +
			"MDS2\n" +
			"MECOM\n" +
			"MED12\n" +
			"MEN1\n" +
			"MET\n" +
			"MITF\n" +
			"MKL1\n" +
			"MLF1\n" +
			"MLH1\n" +
			"MLLT1\n" +
			"MLLT10\n" +
			"MLLT11\n" +
			"MLLT3\n" +
			"MLLT4\n" +
			"MLLT6\n" +
			"MN1\n" +
			"MNX1\n" +
			"MPL\n" +
			"MSH2\n" +
			"MSH6\n" +
			"MSI2\n" +
			"MSN\n" +
			"MTCP1\n" +
			"MTOR\n" +
			"MUC1\n" +
			"MUC16\n" +
			"MUC4\n" +
			"MUTYH\n" +
			"MYB\n" +
			"MYC\n" +
			"MYCL\n" +
			"MYCN\n" +
			"MYD88\n" +
			"MYH11\n" +
			"MYH9\n" +
			"MYO5A\n" +
			"MYOD1\n" +
			"NAB2\n" +
			"NACA\n" +
			"NBEA\n" +
			"NBN\n" +
			"NCKIPSD\n" +
			"NCOA1\n" +
			"NCOA2\n" +
			"NCOA4\n" +
			"NCOR1\n" +
			"NCOR2\n" +
			"NDRG1\n" +
			"NF1\n" +
			"NF2\n" +
			"NFATC2\n" +
			"NFE2L2\n" +
			"NFIB\n" +
			"NFKB2\n" +
			"NFKBIE\n" +
			"NIN\n" +
			"NKX2-1\n" +
			"NONO\n" +
			"NOTCH1\n" +
			"NOTCH2\n" +
			"NPM1\n" +
			"NR4A3\n" +
			"NRAS\n" +
			"NRG1\n" +
			"NSD1\n" +
			"NT5C2\n" +
			"NTHL1\n" +
			"NTRK1\n" +
			"NTRK3\n" +
			"NUMA1\n" +
			"NUP214\n" +
			"NUP98\n" +
			"NUTM1\n" +
			"NUTM2A\n" +
			"NUTM2B\n" +
			"OLIG2\n" +
			"OMD\n" +
			"P2RY8\n" +
			"PABPC1\n" +
			"PAFAH1B2\n" +
			"PALB2\n" +
			"PAX3\n" +
			"PAX5\n" +
			"PAX7\n" +
			"PAX8\n" +
			"PBRM1\n" +
			"PBX1\n" +
			"PCBP1\n" +
			"PCM1\n" +
			"PDCD1LG2\n" +
			"PDE4DIP\n" +
			"PDGFB\n" +
			"PDGFRA\n" +
			"PDGFRB\n" +
			"PER1\n" +
			"PHF6\n" +
			"PHOX2B\n" +
			"PICALM\n" +
			"PIK3CA\n" +
			"PIK3CB\n" +
			"PIK3R1\n" +
			"PIM1\n" +
			"PLAG1\n" +
			"PLCG1\n" +
			"PML\n" +
			"PMS1\n" +
			"PMS2\n" +
			"POLD1\n" +
			"POLE\n" +
			"POLG\n" +
			"POLQ\n" +
			"POT1\n" +
			"POU2AF1\n" +
			"POU5F1\n" +
			"PPARG\n" +
			"PPFIBP1\n" +
			"PPM1D\n" +
			"PPP2R1A\n" +
			"PPP6C\n" +
			"PRCC\n" +
			"PRDM1\n" +
			"PRDM16\n" +
			"PRDM2\n" +
			"PREX2\n" +
			"PRF1\n" +
			"PRKACA\n" +
			"PRKAR1A\n" +
			"PRKCB\n" +
			"PRPF40B\n" +
			"PRRX1\n" +
			"PSIP1\n" +
			"PTCH1\n" +
			"PTEN\n" +
			"PTK6\n" +
			"PTPN11\n" +
			"PTPN13\n" +
			"PTPN6\n" +
			"PTPRB\n" +
			"PTPRC\n" +
			"PTPRK\n" +
			"PTPRT\n" +
			"PWWP2A\n" +
			"QKI\n" +
			"RABEP1\n" +
			"RAC1\n" +
			"RAD21\n" +
			"RAD51B\n" +
			"RAF1\n" +
			"RALGDS\n" +
			"RANBP2\n" +
			"RAP1GDS1\n" +
			"RARA\n" +
			"RB1\n" +
			"RBM10\n" +
			"RBM15\n" +
			"RECQL4\n" +
			"REL\n" +
			"RET\n" +
			"RGPD3\n" +
			"RGS7\n" +
			"RHOA\n" +
			"RHOH\n" +
			"RMI2\n" +
			"RNF213\n" +
			"RNF43\n" +
			"ROBO2\n" +
			"ROS1\n" +
			"RPL10\n" +
			"RPL22\n" +
			"RPL5\n" +
			"RPN1\n" +
			"RSPO2\n" +
			"RSPO3\n" +
			"RUNDC2A\n" +
			"RUNX1\n" +
			"RUNX1T1\n" +
			"S100A7\n" +
			"SALL4\n" +
			"SBDS\n" +
			"SDC4\n" +
			"SDHA\n" +
			"SDHAF2\n" +
			"SDHB\n" +
			"SDHC\n" +
			"SDHD\n" +
			"SEPT5\n" +
			"SEPT6\n" +
			"SEPT9\n" +
			"SET\n" +
			"SETBP1\n" +
			"SETD2\n" +
			"SF3B1\n" +
			"SFPQ\n" +
			"SFRP4\n" +
			"SGK1\n" +
			"SH2B3\n" +
			"SH3GL1\n" +
			"SIRPA\n" +
			"SKI\n" +
			"SLC34A2\n" +
			"SLC45A3\n" +
			"SMAD2\n" +
			"SMAD3\n" +
			"SMAD4\n" +
			"SMARCA4\n" +
			"SMARCB1\n" +
			"SMARCD1\n" +
			"SMARCE1\n" +
			"SMC1A\n" +
			"SMO\n" +
			"SND1\n" +
			"SOCS1\n" +
			"SOX2\n" +
			"SPECC1\n" +
			"SPEN\n" +
			"SPOP\n" +
			"SRC\n" +
			"SRGAP3\n" +
			"SRSF2\n" +
			"SRSF3\n" +
			"SS18\n" +
			"SS18L1\n" +
			"SSX1\n" +
			"SSX2\n" +
			"SSX4\n" +
			"STAG1\n" +
			"STAG2\n" +
			"STAT3\n" +
			"STAT5B\n" +
			"STAT6\n" +
			"STIL\n" +
			"STK11\n" +
			"STRN\n" +
			"SUFU\n" +
			"SUZ12\n" +
			"SYK\n" +
			"TAF15\n" +
			"TAL1\n" +
			"TAL2\n" +
			"TBL1XR1\n" +
			"TBX3\n" +
			"TCEA1\n" +
			"TCF12\n" +
			"TCF3\n" +
			"TCF7L2\n" +
			"TCL1A\n" +
			"TEC\n" +
			"TERT\n" +
			"TET1\n" +
			"TET2\n" +
			"TFE3\n" +
			"TFEB\n" +
			"TFG\n" +
			"TFPT\n" +
			"TFRC\n" +
			"TGFBR2\n" +
			"THRAP3\n" +
			"TLX1\n" +
			"TLX3\n" +
			"TMEM127\n" +
			"TMPRSS2\n" +
			"TNC\n" +
			"TNFAIP3\n" +
			"TNFRSF14\n" +
			"TNFRSF17\n" +
			"TOP1\n" +
			"TP53\n" +
			"TP63\n" +
			"TPM3\n" +
			"TPM4\n" +
			"TPR\n" +
			"TRA\n" +
			"TRAF7\n" +
			"TRB\n" +
			"TRD\n" +
			"TRIM24\n" +
			"TRIM27\n" +
			"TRIM33\n" +
			"TRIP11\n" +
			"TRRAP\n" +
			"TSC1\n" +
			"TSC2\n" +
			"TSHR\n" +
			"U2AF1\n" +
			"UBR5\n" +
			"USP6\n" +
			"USP8\n" +
			"VAV1\n" +
			"VHL\n" +
			"VTI1A\n" +
			"WAS\n" +
			"WHSC1\n" +
			"WHSC1L1\n" +
			"WIF1\n" +
			"WNK2\n" +
			"WRN\n" +
			"WT1\n" +
			"WWTR1\n" +
			"XPA\n" +
			"XPC\n" +
			"XPO1\n" +
			"YWHAE\n" +
			"ZBTB16\n" +
			"ZCCHC8\n" +
			"ZEB1\n" +
			"ZFHX3\n" +
			"ZMYM3\n" +
			"ZNF198\n" +
			"ZNF278\n" +
			"ZNF331\n" +
			"ZNF384\n" +
			"ZNF429\n" +
			"ZNF479\n" +
			"ZNF521\n" +
			"ZNRF3\n" +
			"ZRSR2\n" +
			"ABL1\n" +
			"ABL2\n" +
			"ACTB\n" +
			"ACTG1\n" +
			"ACVR1\n" +
			"ACVR1B\n" +
			"ACVR2A\n" +
			"PARP1\n" +
			"AKT1\n" +
			"AKT2\n" +
			"ALDH2\n" +
			"ALK\n" +
			"ALOX12B\n" +
			"APC\n" +
			"BIRC3\n" +
			"XIAP\n" +
			"FAS\n" +
			"AR\n" +
			"ARAF\n" +
			"RHOA\n" +
			"RHOH\n" +
			"ARNT\n" +
			"ZFHX3\n" +
			"ATF1\n" +
			"ATIC\n" +
			"ATM\n" +
			"ATP1A1\n" +
			"ATP2B3\n" +
			"ATP6V1B2\n" +
			"ATP6AP1\n" +
			"ATR\n" +
			"ATRX\n" +
			"AXL\n" +
			"B2M\n" +
			"BARD1\n" +
			"CCND1\n" +
			"BCL2\n" +
			"BCL2L1\n" +
			"BCL2L2\n" +
			"BCL3\n" +
			"BCL5\n" +
			"BCL6\n" +
			"BCL7A\n" +
			"NBEAP1\n" +
			"BCL9\n" +
			"TNFRSF17\n" +
			"BCR\n" +
			"PRDM1\n" +
			"BLM\n" +
			"BMPR1A\n" +
			"FOXL2\n" +
			"BRCA1\n" +
			"BRAF\n" +
			"BRCA2\n" +
			"BTG1\n" +
			"BTK\n" +
			"BUB1B\n" +
			"CACNA1D\n" +
			"CAD\n" +
			"CALR\n" +
			"CARS\n" +
			"CASP8\n" +
			"RUNX2\n" +
			"RUNX1\n" +
			"RUNX1T1\n" +
			"CBFA2T3\n" +
			"CBFB\n" +
			"CBL\n" +
			"CBLB\n" +
			"CCND2\n" +
			"CCND3\n" +
			"CCNE1\n" +
			"CD22\n" +
			"CD28\n" +
			"CD36\n" +
			"CD58\n" +
			"CD70\n" +
			"CD74\n" +
			"CD79A\n" +
			"CD79B\n" +
			"CDC42\n" +
			"CDH1\n" +
			"CDH11\n" +
			"CDK4\n" +
			"CDK6\n" +
			"CDK8\n" +
			"CDKN1A\n" +
			"CDKN1B\n" +
			"CDKN2A\n" +
			"CDKN2B\n" +
			"CDKN2C\n" +
			"CDX2\n" +
			"CEBPA\n" +
			"CENPA\n" +
			"CHD2\n" +
			"CHD4\n" +
			"AKR1C4\n" +
			"CHEK1\n" +
			"CHN1\n" +
			"CKS1B\n" +
			"CLTC\n" +
			"COL1A1\n" +
			"COL2A1\n" +
			"KLF6\n" +
			"COX6C\n" +
			"CPS1\n" +
			"CREB1\n" +
			"CREBBP\n" +
			"CRKL\n" +
			"CSF1\n" +
			"CSF1R\n" +
			"CSF3R\n" +
			"CTLA4\n" +
			"CTNNA1\n" +
			"CTNNB1\n" +
			"CUX1\n" +
			"CYLD\n" +
			"DAXX\n" +
			"DCTN1\n" +
			"DDB2\n" +
			"DDIT3\n" +
			"DDX3X\n" +
			"DDX5\n" +
			"DDX6\n" +
			"DDX10\n" +
			"DNM2\n" +
			"DNMT1\n" +
			"DNMT3A\n" +
			"DNMT3B\n" +
			"ARID3A\n" +
			"DTX1\n" +
			"DUSP2\n" +
			"DUSP4\n" +
			"DUSP9\n" +
			"E2F3\n" +
			"EBF1\n" +
			"EGFR\n" +
			"EGR1\n" +
			"EIF1AX\n" +
			"EIF4A2\n" +
			"EIF4E\n" +
			"ELF3\n" +
			"ELF4\n" +
			"ELK4\n" +
			"ELN\n" +
			"EP300\n" +
			"EPAS1\n" +
			"EPHA3\n" +
			"EPHA5\n" +
			"EPHA7\n" +
			"EPHB1\n" +
			"EPOR\n" +
			"EPS15\n" +
			"ERBB2\n" +
			"ERBB3\n" +
			"ERBB4\n" +
			"ERCC2\n" +
			"ERCC3\n" +
			"ERCC4\n" +
			"ERCC5\n" +
			"ERF\n" +
			"ERG\n" +
			"ESR1\n" +
			"ETS1\n" +
			"ETV1\n" +
			"ETV4\n" +
			"ETV5\n" +
			"ETV6\n" +
			"MECOM\n" +
			"EWSR1\n" +
			"EXT1\n" +
			"EXT2\n" +
			"EZH1\n" +
			"EZH2\n" +
			"FANCA\n" +
			"FANCC\n" +
			"FANCD2\n" +
			"FANCE\n" +
			"ACSL3\n" +
			"FANCF\n" +
			"FANCG\n" +
			"FAT1\n" +
			"FCGR2B\n" +
			"FES\n" +
			"FGF3\n" +
			"FGF4\n" +
			"FGF6\n" +
			"FGF10\n" +
			"FGF14\n" +
			"FGFR1\n" +
			"FGFR3\n" +
			"FGFR2\n" +
			"FGFR4\n" +
			"FH\n" +
			"FHIT\n" +
			"FOXO1\n" +
			"FOXO3\n" +
			"FLI1\n" +
			"FLT1\n" +
			"FLT3\n" +
			"FLT4\n" +
			"MTOR\n" +
			"FUS\n" +
			"KDSR\n" +
			"FYN\n" +
			"GABRA6\n" +
			"GATA1\n" +
			"GATA2\n" +
			"GATA3\n" +
			"GATA4\n" +
			"GATA6\n" +
			"GPC3\n" +
			"GLI1\n" +
			"GNA11\n" +
			"GNA12\n" +
			"GNAQ\n" +
			"GNAS\n" +
			"GNB1\n" +
			"GPS2\n" +
			"GRIN2A\n" +
			"GRM3\n" +
			"GSK3B\n" +
			"MSH6\n" +
			"GTF2I\n" +
			"HIST1H1C\n" +
			"HIST1H1D\n" +
			"HIST1H1E\n" +
			"HIST1H1B\n" +
			"HIST1H2BD\n" +
			"H3F3A\n" +
			"H3F3B\n" +
			"HDAC1\n" +
			"HGF\n" +
			"NRG1\n" +
			"HIF1A\n" +
			"HIP1\n" +
			"HLA-A\n" +
			"HLA-B\n" +
			"MNX1\n" +
			"HLF\n" +
			"HMGA1\n" +
			"FOXA1\n" +
			"HNRNPA2B1\n" +
			"TLX1\n" +
			"HOXA3\n" +
			"HOXA9\n" +
			"HOXA11\n" +
			"HOXA13\n" +
			"HOXC11\n" +
			"HOXC13\n" +
			"HOXD11\n" +
			"HOXD13\n" +
			"HRAS\n" +
			"HSD3B1\n" +
			"HSP90AA1\n" +
			"HSP90AB1\n" +
			"DNAJB1\n" +
			"IRF8\n" +
			"ID3\n" +
			"IDH1\n" +
			"IDH2\n" +
			"IFNGR1\n" +
			"IGF1\n" +
			"IGF1R\n" +
			"IGF2\n" +
			"IGH\n" +
			"IGL\n" +
			"IKBKB\n" +
			"IL2\n" +
			"IL3\n" +
			"IL6ST\n" +
			"IL7R\n" +
			"IL10\n" +
			"INHA\n" +
			"INHBA\n" +
			"INPP4A\n" +
			"INPP5D\n" +
			"INPPL1\n" +
			"INSR\n" +
			"EIF3E\n" +
			"IRF1\n" +
			"IRF2\n" +
			"IRF4\n" +
			"IRS1\n" +
			"ITK\n" +
			"JAK1\n" +
			"JAK2\n" +
			"JAK3\n" +
			"JARID2\n" +
			"JUN\n" +
			"KCNJ5\n" +
			"KDR\n" +
			"KEL\n" +
			"KIF5B\n" +
			"KIT\n" +
			"KLK2\n" +
			"KRAS\n" +
			"KTN1\n" +
			"AFF3\n" +
			"LASP1\n" +
			"LCK\n" +
			"LCP1\n" +
			"LIFR\n" +
			"LMNA\n" +
			"LMO1\n" +
			"LMO2\n" +
			"LPP\n" +
			"LTB\n" +
			"LYL1\n" +
			"LYN\n" +
			"SH2D1A\n" +
			"EPCAM\n" +
			"SMAD2\n" +
			"SMAD3\n" +
			"SMAD4\n" +
			"MAF\n" +
			"MAX\n" +
			"MCL1\n" +
			"MDM2\n" +
			"MDM4\n" +
			"MEF2BNB-MEF2B\n" +
			"MEF2C\n" +
			"MAP3K1\n" +
			"MEN1\n" +
			"MET\n" +
			"CIITA\n" +
			"MITF\n" +
			"MKI67\n" +
			"MLF1\n" +
			"MLH1\n" +
			"KMT2A\n" +
			"MLLT1\n" +
			"AFF1\n" +
			"MLLT3\n" +
			"MLLT4\n" +
			"MLLT6\n" +
			"FOXO4\n" +
			"MN1\n" +
			"MPL\n" +
			"MRE11A\n" +
			"MSH2\n" +
			"MSH3\n" +
			"MSI1\n" +
			"MSN\n" +
			"MST1\n" +
			"MST1R\n" +
			"MTCP1\n" +
			"MUC1\n" +
			"MUTYH\n" +
			"MYB\n" +
			"MYC\n" +
			"MYCL\n" +
			"MYCN\n" +
			"MYD88\n" +
			"GADD45B\n" +
			"MYH9\n" +
			"MYH11\n" +
			"MYO5A\n" +
			"MYOD1\n" +
			"NAB2\n" +
			"NACA\n" +
			"NBN\n" +
			"NF1\n" +
			"NF2\n" +
			"NFATC2\n" +
			"NFE2\n" +
			"NFE2L2\n" +
			"NFIB\n" +
			"NFKB2\n" +
			"NFKBIA\n" +
			"NFKBIE\n" +
			"NKX3-1\n" +
			"NONO\n" +
			"CNOT3\n" +
			"NOTCH1\n" +
			"NOTCH2\n" +
			"NOTCH3\n" +
			"NOTCH4\n" +
			"NPM1\n" +
			"NRAS\n" +
			"NTHL1\n" +
			"NTRK1\n" +
			"NTRK2\n" +
			"NTRK3\n" +
			"DDR2\n" +
			"NUMA1\n" +
			"NUP98\n" +
			"OMD\n" +
			"FURIN\n" +
			"PAFAH1B2\n" +
			"PAK1\n" +
			"PAK3\n" +
			"PARK2\n" +
			"PAX3\n" +
			"PAX5\n" +
			"PAX7\n" +
			"PBX1\n" +
			"PC\n" +
			"PCBP1\n" +
			"PCM1\n" +
			"PDCD1\n" +
			"PDGFB\n" +
			"PDGFRA\n" +
			"PDGFRB\n" +
			"PDK1\n" +
			"PDPK1\n" +
			"PER1\n" +
			"PGR\n" +
			"PHF1\n" +
			"PIGA\n" +
			"PIK3C2B\n" +
			"PIK3C2G\n" +
			"PIK3C3\n" +
			"PIK3CA\n" +
			"PIK3CB\n" +
			"PIM1\n" +
			"PIK3CD\n" +
			"PIK3CG\n" +
			"PIK3R1\n" +
			"PIK3R2\n" +
			"PLAG1\n" +
			"PLCG1\n" +
			"PLCG2\n" +
			"PMAIP1\n" +
			"PML\n" +
			"PMS1\n" +
			"PMS2\n" +
			"PRRX1\n" +
			"SEPT5\n" +
			"POLD1\n" +
			"POLE\n" +
			"POU2AF1\n" +
			"POU5F1\n" +
			"PPARG\n" +
			"PPP1CB\n" +
			"PPP2R1A\n" +
			"PPP6C\n" +
			"PRCC\n" +
			"PRF1\n" +
			"PRKACA\n" +
			"PRKAR1A\n" +
			"PRKCI\n" +
			"PRKD1\n" +
			"PRKDC\n" +
			"MAPK1\n" +
			"MAPK3\n" +
			"MAP2K1\n" +
			"MAP2K2\n" +
			"PRSS1\n" +
			"RELN\n" +
			"PRSS8\n" +
			"PTCH1\n" +
			"PTEN\n" +
			"PTK6\n" +
			"PTK7\n" +
			"PTPN1\n" +
			"PTPN2\n" +
			"PTPN6\n" +
			"PTPN11\n" +
			"PTPN13\n" +
			"PTPRB\n" +
			"PTPRC\n" +
			"PTPRD\n" +
			"PTPRK\n" +
			"PTPRO\n" +
			"PTPRS\n" +
			"RAC1\n" +
			"RAC2\n" +
			"RAD21\n" +
			"RAD51\n" +
			"RAD51C\n" +
			"RAD51B\n" +
			"RAD51D\n" +
			"RAD52\n" +
			"RAF1\n" +
			"RALGDS\n" +
			"RANBP2\n" +
			"RAP1GDS1\n" +
			"RARA\n" +
			"RASA1\n" +
			"RB1\n" +
			"ARID4A\n" +
			"KDM5A\n" +
			"RECQL\n" +
			"REL\n" +
			"UPF1\n" +
			"RET\n" +
			"TRIM27\n" +
			"RHEB\n" +
			"RIT1\n" +
			"ROBO1\n" +
			"ROS1\n" +
			"RPL5\n" +
			"RPL10\n" +
			"RPL22\n" +
			"RPN1\n" +
			"RPS6KB2\n" +
			"RRAS\n" +
			"CLIP1\n" +
			"RXRA\n" +
			"ATXN2\n" +
			"SDC4\n" +
			"SDHA\n" +
			"SDHB\n" +
			"SDHC\n" +
			"SDHD\n" +
			"MAP2K4\n" +
			"SET\n" +
			"SFRP4\n" +
			"SRSF2\n" +
			"SRSF3\n" +
			"SGK1\n" +
			"SH3GL1\n" +
			"STIL\n" +
			"SLC1A2\n" +
			"SMARCA1\n" +
			"SMARCA4\n" +
			"SMARCB1\n" +
			"SMARCD1\n" +
			"SMARCE1\n" +
			"SMO\n" +
			"SOS1\n" +
			"SOX2\n" +
			"SOX9\n" +
			"SOX10\n" +
			"SPTA1\n" +
			"SRC\n" +
			"SSX1\n" +
			"SSX2\n" +
			"SSX4\n" +
			"SS18\n" +
			"STAT3\n" +
			"STAT4\n" +
			"STAT5A\n" +
			"STAT5B\n" +
			"STAT6\n" +
			"AURKA\n" +
			"STK11\n" +
			"STRN\n" +
			"SYK\n" +
			"TAF1\n" +
			"MAP3K7\n" +
			"TAL1\n" +
			"TAL2\n" +
			"TAP1\n" +
			"TAP2\n" +
			"TCEA1\n" +
			"TCEB1\n" +
			"TBX3\n" +
			"HNF1A\n" +
			"TCF3\n" +
			"TCF7L2\n" +
			"TCF12\n" +
			"TRA\n" +
			"TRB\n" +
			"TRD\n" +
			"TRG\n" +
			"TEK\n" +
			"TERC\n" +
			"TERT\n" +
			"TFE3\n" +
			"TFRC\n" +
			"TGFBR1\n" +
			"TGFBR2\n" +
			"NKX2-1\n" +
			"TLL2\n" +
			"TMPRSS2\n" +
			"TMSB4XP8\n" +
			"TNFAIP3\n" +
			"TOP1\n" +
			"TOP2A\n" +
			"TP53\n" +
			"TP53BP1\n" +
			"TPM3\n" +
			"TPM4\n" +
			"TPR\n" +
			"TRAF2\n" +
			"TRAF3\n" +
			"TRAF5\n" +
			"TSC1\n" +
			"TSC2\n" +
			"TSHR\n" +
			"TYK2\n" +
			"U2AF1\n" +
			"KDM6A\n" +
			"VAV1\n" +
			"VAV2\n" +
			"VEGFA\n" +
			"VHL\n" +
			"EZR\n" +
			"WAS\n" +
			"WHSC1\n" +
			"WRN\n" +
			"WT1\n" +
			"XBP1\n" +
			"XPA\n" +
			"XPC\n" +
			"XPO1\n" +
			"XRCC2\n" +
			"YES1\n" +
			"YWHAE\n" +
			"CNBP\n" +
			"ZNF24\n" +
			"ZBTB16\n" +
			"ZMYM2\n" +
			"ZNF217\n" +
			"PTP4A1\n" +
			"CSDE1\n" +
			"BTG2\n" +
			"PAX8\n" +
			"CXCR4\n" +
			"DEK\n" +
			"TFEB\n" +
			"RNF217-AS1\n" +
			"TUSC3\n" +
			"KAT6A\n" +
			"NR4A3\n" +
			"BRD3\n" +
			"NUP214\n" +
			"MLLT10\n" +
			"CCDC6\n" +
			"NCOA4\n" +
			"SHOC2\n" +
			"FGF23\n" +
			"KMT2D\n" +
			"HMGA2\n" +
			"TCL1A\n" +
			"TAF15\n" +
			"ELL\n" +
			"NCOA3\n" +
			"LZTR1\n" +
			"CLTCL1\n" +
			"ZRSR2\n" +
			"RBM10\n" +
			"KDM5C\n" +
			"SMC1A\n" +
			"ARID1A\n" +
			"HIST3H3\n" +
			"HIST1H4I\n" +
			"TRRAP\n" +
			"PICALM\n" +
			"AXIN1\n" +
			"AXIN2\n" +
			"BAP1\n" +
			"HIST1H2AL\n" +
			"HIST1H2AC\n" +
			"HIST1H2AM\n" +
			"HIST1H2BG\n" +
			"HIST1H2BC\n" +
			"HIST1H2BO\n" +
			"HIST1H3A\n" +
			"HIST1H3D\n" +
			"HIST1H3C\n" +
			"HIST1H3E\n" +
			"HIST1H3I\n" +
			"HIST1H3G\n" +
			"HIST1H3J\n" +
			"HIST1H3H\n" +
			"HIST1H3B\n" +
			"SPOP\n" +
			"RAD54L\n" +
			"CUL3\n" +
			"PPM1D\n" +
			"PPFIBP1\n" +
			"PIK3R3\n" +
			"GAS7\n" +
			"ASMTL\n" +
			"TP63\n" +
			"NCOA1\n" +
			"SOCS1\n" +
			"IRS2\n" +
			"EED\n" +
			"TNFRSF14\n" +
			"TNFRSF11A\n" +
			"TRIM24\n" +
			"INPP4B\n" +
			"GMPS\n" +
			"SOCS2\n" +
			"WISP3\n" +
			"STK19\n" +
			"FUBP1\n" +
			"BCL10\n" +
			"PHOX2B\n" +
			"HIST1H3F\n" +
			"HIST1H2AG\n" +
			"HIST1H2BJ\n" +
			"MGAM\n" +
			"RPS6KA4\n" +
			"MAP3K14\n" +
			"SOCS3\n" +
			"MAP3K6\n" +
			"USP6\n" +
			"USP8\n" +
			"LATS1\n" +
			"SMC3\n" +
			"RABEP1\n" +
			"PCSK7\n" +
			"MAP3K13\n" +
			"ZMYM3\n" +
			"AURKB\n" +
			"S1PR2\n" +
			"KLF4\n" +
			"TRIP11\n" +
			"SLIT2\n" +
			"RECQL4\n" +
			"QKI\n" +
			"ATG5\n" +
			"MAGED1\n" +
			"APOBEC3B\n" +
			"NCOR1\n" +
			"NCOR2\n" +
			"SNCAIP\n" +
			"IKBKE\n" +
			"MDC1\n" +
			"PDE4DIP\n" +
			"NUP93\n" +
			"HERPUD1\n" +
			"FAM131B\n" +
			"SETD1A\n" +
			"KMT2B\n" +
			"HDAC4\n" +
			"KEAP1\n" +
			"MAGI2\n" +
			"SETDB1\n" +
			"SRGAP3\n" +
			"MAFB\n" +
			"GOLGA5\n" +
			"FGF19\n" +
			"THRAP3\n" +
			"MED12\n" +
			"AKT3\n" +
			"ABI1\n" +
			"BCL2L11\n" +
			"SH2B3\n" +
			"RAD50\n" +
			"ARFRP1\n" +
			"AKAP9\n" +
			"LHFP\n" +
			"OLIG2\n" +
			"GPHN\n" +
			"FSTL3\n" +
			"STAG1\n" +
			"IKZF1\n" +
			"TFG\n" +
			"NOD1\n" +
			"NDRG1\n" +
			"YAP1\n" +
			"HOXB13\n" +
			"CARM1\n" +
			"NCOA2\n" +
			"SLC34A2\n" +
			"ARID3B\n" +
			"CTCF\n" +
			"GNA13\n" +
			"CCT6B\n" +
			"STAG2\n" +
			"PLK2\n" +
			"SEPT9\n" +
			"FRS2\n" +
			"ARID5A\n" +
			"MALT1\n" +
			"PNRC1\n" +
			"MLLT11\n" +
			"CLP1\n" +
			"RAB35\n" +
			"CNTRL\n" +
			"FGFR1OP\n" +
			"PTPRT\n" +
			"FAF1\n" +
			"PSIP1\n" +
			"WIF1\n" +
			"CHEK2\n" +
			"SP140\n" +
			"U2AF2\n" +
			"RRAS2\n" +
			"IKZF3\n" +
			"IKZF2\n" +
			"ICK\n" +
			"SEC31A\n" +
			"DIS3\n" +
			"DUX4L1\n" +
			"NT5C2\n" +
			"PDCD11\n" +
			"SPEN\n" +
			"PDS5B\n" +
			"FNBP1\n" +
			"SMG1\n" +
			"SETD1B\n" +
			"KDM4C\n" +
			"ERC1\n" +
			"ARHGAP26\n" +
			"CIC\n" +
			"SEPT6\n" +
			"PASK\n" +
			"CAMTA1\n" +
			"MGA\n" +
			"ACSL6\n" +
			"ICOSLG\n" +
			"ARHGEF12\n" +
			"CRTC1\n" +
			"NCSTN\n" +
			"DICER1\n" +
			"RYBP\n" +
			"SF3B1\n" +
			"HEY1\n" +
			"BRD4\n" +
			"SUZ12\n" +
			"KAT6B\n" +
			"PATZ1\n" +
			"CBLC\n" +
			"POT1\n" +
			"ZNF521\n" +
			"WWTR1\n" +
			"SAMHD1\n" +
			"ADGRA2\n" +
			"SS18L1\n" +
			"SETBP1\n" +
			"LSM14A\n" +
			"CHIC2\n" +
			"LATS2\n" +
			"GREM1\n" +
			"TCL6\n" +
			"SND1\n" +
			"FOXP1\n" +
			"BBC3\n" +
			"AFF4\n" +
			"AGO2\n" +
			"SESN1\n" +
			"EML4\n" +
			"PCLO\n" +
			"SETD2\n" +
			"BABAM1\n" +
			"DROSHA\n" +
			"ANKRD11\n" +
			"CD274\n" +
			"TFPT\n" +
			"TLX3\n" +
			"IL21R\n" +
			"IGK\n" +
			"APH1A\n" +
			"SBDS\n" +
			"EGFL7\n" +
			"LEF1\n" +
			"CRBN\n" +
			"NIN\n" +
			"UBR5\n" +
			"GTSE1\n" +
			"NCKIPSD\n" +
			"HDAC7\n" +
			"TRIM33\n" +
			"YPEL5\n" +
			"SUFU\n" +
			"ARID4B\n" +
			"RTEL1\n" +
			"CDK12\n" +
			"BCL11A\n" +
			"LRP1B\n" +
			"SETD4\n" +
			"DCUN1D1\n" +
			"ERRFI1\n" +
			"FEV\n" +
			"TET2\n" +
			"FAM46C\n" +
			"BCOR\n" +
			"RNF43\n" +
			"WHSC1L1\n" +
			"SDHAF2\n" +
			"FANCL\n" +
			"SHQ1\n" +
			"PBRM1\n" +
			"SETD5\n" +
			"YY1AP1\n" +
			"ELP2\n" +
			"ASXL2\n" +
			"FBXW7\n" +
			"ZNF331\n" +
			"ETNK1\n" +
			"ZCCHC8\n" +
			"TMEM127\n" +
			"TMEM30A\n" +
			"PAG1\n" +
			"PAK6\n" +
			"DUSP22\n" +
			"EMSY\n" +
			"ACKR3\n" +
			"CASC5\n" +
			"CYSLTR2\n" +
			"GOPC\n" +
			"PAK7\n" +
			"SALL4\n" +
			"ARID1B\n" +
			"RPTOR\n" +
			"MIB1\n" +
			"MKL1\n" +
			"ZBTB2\n" +
			"EP400\n" +
			"KIAA1549\n" +
			"RNF213\n" +
			"SHTN1\n" +
			"CCNB1IP1\n" +
			"KMT2C\n" +
			"BACH2\n" +
			"BCORL1\n" +
			"PRDM16\n" +
			"PRDM14\n" +
			"CRLF2\n" +
			"RRAGC\n" +
			"ARHGEF28\n" +
			"SOX17\n" +
			"NSD1\n" +
			"RFWD2\n" +
			"SMYD3\n" +
			"CREB3L2\n" +
			"RBM15\n" +
			"CRTC3\n" +
			"RANBP17\n" +
			"BCL11B\n" +
			"GID4\n" +
			"ASPSCR1\n" +
			"MAPKAP1\n" +
			"CHCHD7\n" +
			"CDC73\n" +
			"FAT4\n" +
			"VTCN1\n" +
			"TBL1XR1\n" +
			"PALB2\n" +
			"FBXO31\n" +
			"MOB3B\n" +
			"SETD6\n" +
			"ZNF703\n" +
			"FBXO11\n" +
			"PREX2\n" +
			"C2ORF44\n" +
			"TET1\n" +
			"PDCD1LG2\n" +
			"CD276\n" +
			"SETD7\n" +
			"FIP1L1\n" +
			"FCRL4\n" +
			"NUF2\n" +
			"SESN2\n" +
			"SETDB2\n" +
			"STK40\n" +
			"BRIP1\n" +
			"FAM175A\n" +
			"ARID5B\n" +
			"SETD3\n" +
			"TRAF7\n" +
			"FLYWCH1\n" +
			"PHF6\n" +
			"HOOK3\n" +
			"CARD11\n" +
			"MAML2\n" +
			"DOT1L\n" +
			"BRSK1\n" +
			"SLX4\n" +
			"KDM2B\n" +
			"RSPO3\n" +
			"CEP89\n" +
			"HIST1H2BK\n" +
			"SLC45A3\n" +
			"KLHL6\n" +
			"KNSTRN\n" +
			"CREB3L1\n" +
			"FAM58A\n" +
			"SNX29\n" +
			"SPECC1\n" +
			"C12orf9\n" +
			"PWWP2A\n" +
			"RMI2\n" +
			"EXOSC6\n" +
			"LRRK2\n" +
			"LRIG3\n" +
			"MSI2\n" +
			"CANT1\n" +
			"HIST2H3C\n" +
			"ARID3C\n" +
			"AMER1\n" +
			"VTI1A\n" +
			"SESN3\n" +
			"C15ORF65\n" +
			"TTL\n" +
			"BTLA\n" +
			"PPP4R2\n" +
			"ESCO2\n" +
			"SPRED1\n" +
			"ZNF384\n" +
			"ASXL1\n" +
			"ARID2\n" +
			"WDR90\n" +
			"TET3\n" +
			"FLCN\n" +
			"MPEG1\n" +
			"RASGEF1A\n" +
			"JAZF1\n" +
			"RICTOR\n" +
			"NUTM1\n" +
			"NEGR1\n" +
			"MDS2\n" +
			"BCL9L\n" +
			"KSR2\n" +
			"HMGN2P46\n" +
			"P2RY8\n" +
			"HIST2H3A\n" +
			"RSPO2\n" +
			"ECT2L\n" +
			"MALAT1\n" +
			"KMT5A\n" +
			"SERP2\n" +
			"MYO18A\n" +
			"H3F3C\n" +
			"H3F3AP4\n" +
			"HIST2H3D\n" +
			"SFPQ\n" +
			"NUTM2A\n" +
			"NUTM2B\n" +
			"TEC\n" +
			"MEF2B").split("\n")));

	public static void main(String[] args)
	{
		System.out.println("genes = " + genes.size());
	}
}
