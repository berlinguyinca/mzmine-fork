# Introduction #

The RawdataDirectoryImportModule allows you to import all the contents of a directory at once instead of having to add every file singly to the batch file. It's not really needed for the normal interaction, but highly useful in batch files.

# Details #

Please be aware that this module only works for the common rawdata files. Like mzData, mzXML and netcdf.

# Usage #

```

  <batchstep method="net.sf.mzmine.modules.rawdatamethods.rawdataimport.RawDataDirectoryImportModule">
        <parameter name="Raw data file names">
                    <file>MY_DIRECTOR_OF_CHOICE</file>
        </parameter>
    </batchstep>

```