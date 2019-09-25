# Azure RangeReader

The AWS S3 RangeReader implementation allows users to read COGs from Amazon's S3 storage service.  It supports both 
`http` and `s3` style URLs with the following syntax:

 * http[s]://s3-<region>.amazonaws.com/\<bucket>/\<key>
 * s3://\<bucket>/\<key>
 
 When using the `s3` syntax, the region must be provided via an environment variable, system property, or the URL 
 parameter `region`:
 
 * s3://\<bucket>/\<key>?region=\<region>
 
 The following system properties/environment variables are available for configuration/authentication:
 
 | System Property | Environment Variable | Description |
 | --------------- | -------------------- | ----------- |
 | \<alias>.aws.user | \<ALIAS>_AWS_USER    | The S3 user name |
 | \<alias>.aws.password | \<ALIAS>_AWS_PASSWORD| The S3 password |
 | \<alias>.aws.endpoint | \<ALIAS>_AWS_ENDPIONT| The S3 endpoint  |
 | \<alias>.aws.region | \<ALIAS>_AWS_REGION    | The S3 region |
 
 The alias value for a given request is determined by the protocol of the URL. Any number of aliases may be configured 
 in order to support 3rd party S3 compatible services.
 
 If no credentials are provided, the AWS client will use the 
 [default credential provider chain](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html#using-the-default-credential-provider-chain).
 