# Introduction #

File format description for LECO ChromaTOF deconvoluted CSV files.

# Details #

All entries are separated by a comma and all entries (which may contain non-delimiter commas) are enclosed in double quotes.

| **Column Name** | **Description** | **Type** |
|:----------------|:----------------|:---------|
| Name | Annotation of molecular name(s) in this spectrum | String |
| R.T. (s) | Retention time in seconds | Double |
| Type |  |  |
| UniqueMass |  | Integer |
| Concentration |  |  |
| Sample Concentration |  |  |
| Match |  |  |
| Quant Masses |  | Integer Array (delimited by "+") |
| Quant S/N |  | Double |
| Area |  | Double |
| Baseline Modified |  |  |
| Quantification |  |  |
| Spectra | Mass spectrum data | Integer Map (key/value delimited by ":", and each pair delimited by a space) |