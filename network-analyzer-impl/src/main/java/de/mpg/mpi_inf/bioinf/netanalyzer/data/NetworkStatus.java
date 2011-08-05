/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.data;

import javax.swing.ImageIcon;

/**
 * Enumeration on edge construction principles of networks, as well as options (in human readable
 * form) for interpretations.
 * 
 * @author Yassen Assenov
 */
public class NetworkStatus {

	/**
	 * Images used to depict network statuses.
	 */
	private static final ImageIcon[] allIcons = new ImageIcon[] { Utils.getImage("00.png", ""),
		Utils.getImage("01.png", ""), Utils.getImage("02.png", ""), Utils.getImage("03.png", ""),
		Utils.getImage("04.png", ""), Utils.getImage("05.png", ""), Utils.getImage("06.png", ""),
		Utils.getImage("07.png", ""), Utils.getImage("08.png", ""), Utils.getImage("09.png", ""),
		Utils.getImage("10.png", ""), Utils.getImage("11.png", ""), Utils.getImage("12.png", ""),
		Utils.getImage("13.png", ""), Utils.getImage("14.png", ""), Utils.getImage("15.png", ""),
		Utils.getImage("16.png", ""), Utils.getImage("17.png", ""), Utils.getImage("18.png", ""),
		Utils.getImage("19.png", ""), Utils.getImage("20.png", ""), Utils.getImage("21.png", ""),
		Utils.getImage("22.png", ""), Utils.getImage("23.png", ""), Utils.getImage("24.png", ""),
		Utils.getImage("25.png", ""), Utils.getImage("26.png", ""), Utils.getImage("27.png", ""),
		Utils.getImage("28.png", ""), Utils.getImage("29.png", "") };

	/**
	 * HTML tag for line break. Used in generating interpretation descriptions.
	 */
	private static final String BR = "<br>";

	/**
	 * All possible network interpretations according to the different network statuses.
	 */
	private static final NetworkInterpretation[] allTrs = new NetworkInterpretation[33];

	static {
		// dir_paired_n_n :
		allTrs[0] = new NetworkInterpretation(allIcons[0], true);
		allTrs[1] = new NetworkInterpretation(allIcons[26], false, true);
		allTrs[2] = new NetworkInterpretation(allIcons[1], false, false);
		// dir_paired_s_n :
		allTrs[3] = new NetworkInterpretation(allIcons[2], true);
		allTrs[4] = new NetworkInterpretation(allIcons[3], false, true);
		allTrs[5] = new NetworkInterpretation(allIcons[4], false, false);
		// dir_paired_n_s :
		allTrs[6] = new NetworkInterpretation(allIcons[0], true, true);
		allTrs[7] = new NetworkInterpretation(allIcons[28], false, true);
		allTrs[8] = new NetworkInterpretation(allIcons[6], false, false);
		// dir_paired_s_s :
		allTrs[9] = new NetworkInterpretation(allIcons[2], true, true);
		allTrs[10] = new NetworkInterpretation(allIcons[8], false, true);
		allTrs[11] = new NetworkInterpretation(allIcons[9], false, false);
		// dir_unpaired_n_n :
		allTrs[12] = new NetworkInterpretation(allIcons[10], true);
		allTrs[13] = new NetworkInterpretation(allIcons[26], false);
		// dir_unpaired_s_n :
		allTrs[14] = new NetworkInterpretation(allIcons[11], true);
		allTrs[15] = new NetworkInterpretation(allIcons[3], false);
		// dir_unpaired_n_s :
		allTrs[16] = new NetworkInterpretation(allIcons[10], true, true);
		allTrs[17] = new NetworkInterpretation(allIcons[28], false);
		// dir_unpaired_s_s :
		allTrs[18] = new NetworkInterpretation(allIcons[11], true, true);
		allTrs[19] = new NetworkInterpretation(allIcons[8], false);
		// mixed_paired_n_n :
		allTrs[20] = new NetworkInterpretation(allIcons[26], false, true);
		allTrs[21] = new NetworkInterpretation(allIcons[15], false, false);
		// mixed_paired_s_n :
		allTrs[22] = new NetworkInterpretation(allIcons[3], false, true);
		allTrs[23] = new NetworkInterpretation(allIcons[17], false, false);
		// mixed_paired_n_s :
		allTrs[24] = new NetworkInterpretation(allIcons[28], false, true);
		allTrs[25] = new NetworkInterpretation(allIcons[19], false, false);
		// mixed_paired_s_s :
		allTrs[26] = new NetworkInterpretation(allIcons[8], false, true);
		allTrs[27] = new NetworkInterpretation(allIcons[21], false, false);
		// mixed_unpaired_n_n :
		allTrs[28] = new NetworkInterpretation(allIcons[26], Messages.NI_FORCETU, false);
		// mixed_unpaired_s_n :
		allTrs[29] = new NetworkInterpretation(allIcons[3], Messages.NI_FORCETU, false);
		// mixed_unpaired_n_s :
		allTrs[30] = new NetworkInterpretation(allIcons[28], Messages.NI_FORCETU, false);
		// mixed_unpaired_s_s :
		allTrs[31] = new NetworkInterpretation(allIcons[8], Messages.NI_FORCETU, false);
		// undir_n_n :
		allTrs[32] = new NetworkInterpretation(null, Messages.NI_FORCETU, false);
		// undir_s_n : allTrs[29]
		// undir_n_s : allTrs[32]
		// undir_s_s : allTrs[31]
	}

	/**
	 * A network containing only directed edges which are paired, no self-loops.
	 */
	public static final NetworkStatus DIR_PAIRED_N_N = new NetworkStatus(Messages.NI_DIRPAIRED,
		allIcons[0], allTrs[0], allTrs[1], allTrs[2], 1);

	/**
	 * A network containing only directed edges which are paired, plus directed (paired) self-loops.
	 */
	public static final NetworkStatus DIR_PAIRED_S_N = new NetworkStatus(Messages.NI_DIRPAIRED + BR
		+ Messages.NI_LOOPSDIR, allIcons[2], allTrs[3], allTrs[4], allTrs[5], 1);

	/**
	 * A network containing only directed edges which are paired, plus undirected self-loops.
	 */
	public static final NetworkStatus DIR_PAIRED_N_S = new NetworkStatus(Messages.NI_DIRPAIRED + BR
		+ Messages.NI_LOOPSUNDIR, allIcons[5], allTrs[6], allTrs[7], allTrs[8], 1);

	/**
	 * A network containing only directed edges which are paired, plus both directed and undirected
	 * self-loops.
	 */
	public static final NetworkStatus DIR_PAIRED_S_S = new NetworkStatus(Messages.NI_DIRPAIRED + BR
		+ Messages.NI_LOOPSBOTH, allIcons[7], allTrs[9], allTrs[10], allTrs[11], 1);

	/**
	 * A network containing only directed edges which are not paired, and no self-loops.
	 */
	public static final NetworkStatus DIR_UNPAIRED_N_N = new NetworkStatus(Messages.NI_DIRUNPAIRED,
		allIcons[10], allTrs[12], allTrs[13], 1);

	/**
	 * A network containing only directed edges which are not paired, plus directed self-loops.
	 */
	public static final NetworkStatus DIR_UNPAIRED_S_N = new NetworkStatus(Messages.NI_DIRUNPAIRED
		+ BR + Messages.NI_LOOPSDIR, allIcons[11], allTrs[14], allTrs[15], 1);

	/**
	 * A network containing only directed edges which are not paired, plus undirected self-loops.
	 */
	public static final NetworkStatus DIR_UNPAIRED_N_S = new NetworkStatus(Messages.NI_DIRUNPAIRED
		+ BR + Messages.NI_LOOPSUNDIR, allIcons[12], allTrs[16], allTrs[17], 1);

	/**
	 * A network containing only directed edges which are not paired, plus both directed and
	 * undirected self-loops.
	 */
	public static final NetworkStatus DIR_UNPAIRED_S_S = new NetworkStatus(Messages.NI_DIRUNPAIRED
		+ BR + Messages.NI_LOOPSBOTH, allIcons[13], allTrs[18], allTrs[19], 1);

	/**
	 * A network containing both undirected and paired directed edges, no self-loops.
	 */
	public static final NetworkStatus MIXED_PAIRED_N_N = new NetworkStatus(Messages.NI_MIXED + BR
		+ Messages.NI_PAIRED, allIcons[14], allTrs[20], allTrs[21], 0);

	/**
	 * A network containing both undirected and paired directed edges, plus directed (paired)
	 * self-loops.
	 */
	public static final NetworkStatus MIXED_PAIRED_S_N = new NetworkStatus(Messages.NI_MIXED + BR
		+ Messages.NI_PAIRED + BR + Messages.NI_LOOPSDIR, allIcons[16], allTrs[22], allTrs[23], 0);

	/**
	 * A network containing both undirected and paired directed edges, plus undirected self-loops.
	 */
	public static final NetworkStatus MIXED_PAIRED_N_S = new NetworkStatus(Messages.NI_MIXED + BR
		+ Messages.NI_PAIRED + BR + Messages.NI_LOOPSUNDIR, allIcons[18], allTrs[24], allTrs[25], 0);

	/**
	 * A network containing both undirected and paired directed edges, plus both undirected and
	 * paired directed self-loops.
	 */
	public static final NetworkStatus MIXED_PAIRED_S_S = new NetworkStatus(Messages.NI_MIXED + BR
		+ Messages.NI_UNPAIRED + BR + Messages.NI_LOOPSBOTH, allIcons[20], allTrs[26], allTrs[27],
		0);

	/**
	 * A network containing both undirected and unpaired directed edges, no self-loops.
	 */
	public static final NetworkStatus MIXED_UNPAIRED_N_N = new NetworkStatus(Messages.NI_MIXED + BR
		+ Messages.NI_UNPAIRED, allIcons[22], allTrs[28]);

	/**
	 * A network containing both undirected and unpaired directed edges, plus directed self-loops.
	 */
	public static final NetworkStatus MIXED_UNPAIRED_S_N = new NetworkStatus(Messages.NI_MIXED + BR
		+ Messages.NI_UNPAIRED + BR + Messages.NI_LOOPSDIR, allIcons[23], allTrs[29]);

	/**
	 * A network containing both undirected and unpaired directed edges, plus undirected self-loops.
	 */
	public static final NetworkStatus MIXED_UNPAIRED_N_S = new NetworkStatus(Messages.NI_MIXED + BR
		+ Messages.NI_UNPAIRED + BR + Messages.NI_LOOPSUNDIR, allIcons[24], allTrs[30]);

	/**
	 * A network containing both undirected and unpaired directed edges, plus both undirected and
	 * directed self-loops.
	 */
	public static final NetworkStatus MIXED_UNPAIRED_S_S = new NetworkStatus(Messages.NI_MIXED + BR
		+ Messages.NI_UNPAIRED + BR + Messages.NI_LOOPSBOTH, allIcons[25], allTrs[31]);

	/**
	 * A network containing undirected edges only, and no self-loops.
	 */
	public static final NetworkStatus UNDIR_N_N = new NetworkStatus(Messages.NI_UNDIR,
		allIcons[26], allTrs[32]);

	/**
	 * A network containing undirected edges only, plus directed self-loops.
	 */
	public static final NetworkStatus UNDIR_S_N = new NetworkStatus(Messages.NI_UNDIR + BR
		+ Messages.NI_LOOPSDIR, allIcons[27], allTrs[29]);

	/**
	 * A network containing undirected edges only, plus undirected self-loops.
	 */
	public static final NetworkStatus UNDIR_N_S = new NetworkStatus(Messages.NI_UNDIR + BR
		+ Messages.NI_LOOPSUNDIR, allIcons[28], allTrs[32]);

	/**
	 * A network containing undirected edges only, plus both undirected and directed self-loops.
	 */
	public static final NetworkStatus UNDIR_S_S = new NetworkStatus(Messages.NI_UNDIR + BR
		+ Messages.NI_LOOPSBOTH, allIcons[29], allTrs[31]);

	/**
	 * Gets the network status describing the given boolean network parameters.
	 * <p>
	 * Note that this method never returns <code>null</code>. In particular, networks that
	 * contain neither directed, nor undirected edges, are considered undirected.
	 * </p>
	 * 
	 * @param aInsp Results of inspection on the edges of a network.
	 * @return The <code>NetworkStatus</code> instance that suits best the specified parameters.
	 */
	public static NetworkStatus getStatus(NetworkInspection aInsp) {
		int loops = (aInsp.dirLoops ? 1 : 0) + (aInsp.undirLoops ? 2 : 0);
		if (aInsp.dir) {
			if (aInsp.uniqueDir) {
				if (aInsp.undir) {
					// Status: mixed unpaired
					switch (loops) {
						case 0:
							return NetworkStatus.MIXED_UNPAIRED_N_N;
						case 1:
							return NetworkStatus.MIXED_UNPAIRED_S_N;
						case 2:
							return NetworkStatus.MIXED_UNPAIRED_N_S;
						default:
							return NetworkStatus.MIXED_UNPAIRED_S_S;
					}
				}
				// Status: directed unpaired
				switch (loops) {
					case 0:
						return NetworkStatus.DIR_UNPAIRED_N_N;
					case 1:
						return NetworkStatus.DIR_UNPAIRED_S_N;
					case 2:
						return NetworkStatus.DIR_UNPAIRED_N_S;
					default:
						return NetworkStatus.DIR_UNPAIRED_S_S;
				}

			}
			if (aInsp.undir) {
				// Status: mixed paired
				switch (loops) {
					case 0:
						return NetworkStatus.MIXED_PAIRED_N_N;
					case 1:
						return NetworkStatus.MIXED_PAIRED_S_N;
					case 2:
						return NetworkStatus.MIXED_PAIRED_N_S;
					default:
						return NetworkStatus.MIXED_PAIRED_S_S;
				}
			}
			// Status: directed paired
			switch (loops) {
				case 0:
					return NetworkStatus.DIR_PAIRED_N_N;
				case 1:
					return NetworkStatus.DIR_PAIRED_S_N;
				case 2:
					return NetworkStatus.DIR_PAIRED_N_S;
				default:
					return NetworkStatus.DIR_PAIRED_S_S;
			}
		}
		// Status: undirected
		switch (loops) {
			case 0:
				return NetworkStatus.UNDIR_N_N;
			case 1:
				return NetworkStatus.UNDIR_S_N;
			case 2:
				return NetworkStatus.UNDIR_N_S;
			default:
				return NetworkStatus.UNDIR_S_S;
		}
	}

	/**
	 * Gets the description of this network status.
	 * 
	 * @return Description of the network status in human-readable form.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the image icon of this network status.
	 * 
	 * @return Icon identifying this network status.
	 */
	public ImageIcon getIcon() {
		return icon;
	}

	/**
	 * Gets the interpretations for this status.
	 * 
	 * @return Array containing all possible interpretations of this network status.
	 */
	public NetworkInterpretation[] getInterpretations() {
		return interprs;
	}

	/**
	 * Gets the default interpretation index.
	 * 
	 * @return Index of the default interpretation for this status.
	 * @see #getInterpretations()
	 */
	public int getDefaultInterprIndex() {
		return defaultInterprIndex;
	}

	/**
	 * Initializes a new instance of <code>NetworkStatus</code> with a unique interpretation.
	 * 
	 * @param aDescription Status description in a human-readable format.
	 * @param aIcon Image that schematically represents the status. This should be one of the
	 *        elements of {@link #allIcons}.
	 * @param aInterpr The only network interpretation for this status.
	 */
	private NetworkStatus(String aDescription, ImageIcon aIcon, NetworkInterpretation aInterpr) {
		this(aDescription, aIcon, new NetworkInterpretation[] { aInterpr }, 0);
	}

	/**
	 * Initializes a new instance of <code>NetworkStatus</code> with two interpretations.
	 * 
	 * @param aDescription Status description in a human-readable format.
	 * @param aIcon Image that schematically represents the status. This should be one of the
	 *        elements of {@link #allIcons}.
	 * @param aInterpr1 The first network interpretation for this status.
	 * @param aInterpr2 The second network interpretation for this status.
	 * @param aDefIndex Index of the default interpretation. This value must be <code>0</code> or
	 *        <code>1</code>.
	 */
	private NetworkStatus(String aDescription, ImageIcon aIcon, NetworkInterpretation aInterpr1,
		NetworkInterpretation aInterpr2, int aDefIndex) {
		this(aDescription, aIcon, new NetworkInterpretation[] { aInterpr1, aInterpr2 }, aDefIndex);
	}

	/**
	 * Initializes a new instance of <code>NetworkStatus</code> with three interpretations.
	 * 
	 * @param aDescription Status description in a human-readable format.
	 * @param aIcon Image that schematically represents the status. This should be one of the
	 *        elements of {@link #allIcons}.
	 * @param aInterpr1 The first network interpretation for this status.
	 * @param aInterpr2 The second network interpretation for this status.
	 * @param aInterpr3 The third network interpretation for this status.
	 * @param aDefIndex Index of the default interpretation. This value must be <code>0</code>,
	 *        <code>1</code> or <code>2</code>.
	 */
	private NetworkStatus(String aDescription, ImageIcon aIcon, NetworkInterpretation aInterpr1,
		NetworkInterpretation aInterpr2, NetworkInterpretation aInterpr3, int aDefIndex) {
		this(aDescription, aIcon, new NetworkInterpretation[] { aInterpr1, aInterpr2, aInterpr3 },
			aDefIndex);
	}

	/**
	 * Initializes a new instance of <code>NetworkStatus</code>.
	 * 
	 * @param aDescription Status description in a human-readable format.
	 * @param aIcon Image that schematically represents the status. This should be one of the
	 *        elements of {@link #allIcons}.
	 * @param aInterprs Array of possible interpretations for this status.
	 * @param aDefIndex Index of the default interpretation. This value must point to an element in
	 *        the array of possible interpretations.
	 */
	private NetworkStatus(String aDescription, ImageIcon aIcon, NetworkInterpretation[] aInterprs,
		int aDefIndex) {
		description = "<html><b>" + aDescription + "</b>";
		icon = aIcon;
		interprs = aInterprs;
		defaultInterprIndex = aDefIndex;
	}

	/**
	 * Description of the network status in a human-readable form.
	 */
	private String description;

	/**
	 * Image icon identifying this network status.
	 */
	private ImageIcon icon;

	/**
	 * Array of possible network interpretations.
	 */
	private NetworkInterpretation[] interprs;

	/**
	 * Index of the default interpretation. The default interpretation is obtained by:<br>
	 * <code>interprs[defaultInterprIndex]</code>
	 */
	private int defaultInterprIndex;
}
