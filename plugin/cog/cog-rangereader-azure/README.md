# Azure RangeReader

The Azure RangeReader implementation allows users to read COGs from Azure blob storage.  It supports both `http` and
`wasb` style URLs with the following syntax:

 * wasb[s]://<azure_container>@<storage_account>.blob.core.windows.net/path/image.tif
 * http[s]://<storage_account>.blob.core.windows.net/<azure_container>/path/image.tif
 
 Currently, the only authentication mechanism supported is using Azure connection strings.  A connection string can be 
 configured for each storage account you wish to read from and uses the following syntax:
 
 * Environment variable: <storage_account>_AZURE_CONNECTION_STRING
 * System property: <storage_account>.azure.connection.string
 
 The proper connection string will be chosen at runtime based on the storage account value in the COG's URL. 