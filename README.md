# AWS Util Repository

To download a file, specify the command line arguments in the following manner:
- Arg1: bucket name
- Arg2: file name
- Arg3: last modified date

The latest file, with the given file name, compared to the last modified date will be downloaded.

If for any reason (no file exists with the given name, no file was uploaded before the given date, etc.) the download could not succeed, then no file will be downloaded,
and a message will be displayed on the console.

If the download went successfully, then a **downloaded** folder will appear in the root of the project, with a copy of the specified file.
