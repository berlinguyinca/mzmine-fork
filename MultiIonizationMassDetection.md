# Multi-Ionization Mass Detection #

This work required the addition of 3 new features to MZMine:

### Importing ###
The ability to import data deconvoluted with LECO's ChromaTOF in `Raw data import`.


### Filtering ###
A new filtering method for deconvoluted data with 3 parameters:
  * **C13 Isotope Cut:** A filter to remove C13 isotope ions.  This is done by iterating over each mass spectrum from high m/z to low m/z and comparing successive ions.  If the previous ion has a unitary difference in m/z and its intensity is at least 50% greater (with the default value of 0.5) than the current ion, then we mark the current ion as an isotope ion and remove it from the mass spectrum.
  * **Intensity Threshold:** Removes all ions with an intensity less than a given value (default 50 counts).
  * **Base Peak Threshold:** Removes all ions with an intensity less than a given percentage of the current spectrum's base peak intensity (default 0.01, or 1%).

An option is also provided to remove the unfiltered data files from the workspace after filtering is completed.


### Automated Mass Detection ###
Currently, mass detection between only PCI-Methane and PCI-Isobutane spectra is working fully.  We additionally assume the data has been fully aligned or that they were taken under sufficiently similar conditions.

The process is started by selecting all of the required files from the `Raw data files` list and then select `Automated Mass Detection` from the `Raw data methods -> Deconvoluted Data Analysis` menu.

This module has the following parameters
  * **Methane Files:** Select all of the PCI-Methane data files (tries to automatically detect the correct files)
  * **Required Methane Adducts:** The number of adducts that need to be matched in the PCI-Methane spectra to be considered a candidate mass (8 library adducts total)
  * **Isobutane Files:** Select all of the PCI-Isobutane data files (tries to automatically detect the correct files)
  * **Required Isobutane Adducts:** The number of adducts that need to be matched in the PCI-Isobutane spectra to be considered a candidate mass (5 library adducts total)
  * **Required Methane Files:** Number of PCI-Methane data files a mass must be found in to be considered a candidate mass
  * **Required Isobutane Files:** Number of PCI-Isobutane data files a mass must be found in to be considered a candidate mass
  * **Retention Time Search Window:** Maximum retention time window (in seconds) between individual data files in which to match mass candidates
  * **Require that no ion exists at `[M]`:** If selected, this filter excludes all mass candidates that have an ion at m/z = M in any of the matched spectra


# Adducts / Losses #

The following adducts/losses are defined:

### EI: ###

  * `[M]+` : +0
  * `[M-CH3]+` : -15
  * `[M-H2O]+` : -18
  * `[M-OTMS]+` : -89
  * `[M-OTMS_2]+` : -178

### PCI-Methane: ###

  * `[M-TMSOH+H]+` : -89
  * `[M-H2O+H]+` : -17
  * `[M-CH4+H]+` : -15
  * `[M-H]+` : -1
  * `[M+H]+` : +1
  * `[M+CH5]+` : +17
  * `[M+C2H5]+` : +29
  * `[M+C3H5]+` : +41

### PCI-Isobutane: ###

  * `[M-TMSOH+H]+` : -89
  * `[M-H2O+H]+` : -17
  * `[M+H]+` : +1
  * `[M+C3H3]+` : +39
  * `[M+C4H9]+` : +57


# Mass Detection Algorithm #

The algorithm is split into two parts:

### Data File Processing ###

For each mass spectrum within each data file, iterate over all possible mass values (we choose 1 to 1000, inclusive).  For each mass, `m`:
  * If the parameter `Require that no ion exists at [M]` was selected, and if there exists an ion at mass `m`, then skip.
  * Otherwise. count the number of adduct/loss ions that exist about mass `m`.  If the number matches or exceeds the `Required Adducts` threshold, store `m` as a  potential mass candidate.

### Mass Candidate Comparison ###

  1. Sort out the potential mass candidates, collecting candidates together with the same mass value
  1. For each potential mass candidate, produce a retention time search window of the given value about each
  1. For each sorted collection of masses, combine all of the retention time windows that overlap.
  1. For each mass `m` now have a collection of retention time search windows each containing at least 1 mass.  Iterating over each of these, remove any that do not contain the minimal number of `Required Methane Files` and `Required Isobutane Files`.
  1. Each collection of retention time search windows now acts as a single mass candidate, containing information from each data file it was found in.