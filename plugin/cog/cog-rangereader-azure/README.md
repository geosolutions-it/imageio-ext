# Azure RangeReader

The Azure RangeReader implementation allows users to read COGs from Azure's Blobs.
The following system properties are available for configuration/authentication:
 
 | System Property | Description |
 | --------------- | ----------- |
 | azure.reader.accountName | The Azure account name |
 | azure.reader.accountKey | The Azure account key |
 | azure.reader.sasToken | The Azure Shared Access Signature token. Takes precedence over the account key; a leading `?` is optional |
 | azure.reader.container | The Azure container for the blobs |
 | azure.reader.prefix | The optional prefix folder for the blobs |

A container-scoped service SAS with read (`r`) permission is sufficient for COG range reads.
