import datetime, time, dropbox

with open('readings_sample.txt') as f:
    content = f.readlines()

power = 0

for line in content:
    line=line.split(',')
    if len(line) == 4:
        # print line
        power = float(line[1])
        date_time = line[2]+"_"+line[3].rstrip()
        date_time = time.mktime(datetime.
                        datetime.
                        strptime(date_time,
                                 '%m_%d_%Y_%H_%M_%S').
                        timetuple())
        date_time = date_time*1000
        # for js

        # print power, date_time

import dropbox
import os
import datetime
import time

# pp_key = 'xu3vh280gt2moms'
# app_secret = '5rbg9daxi86nqar'
# flow = dropbox.client.DropboxOAuth2FlowNoRedirect(app_key, app_secret)
# client = dropbox.client.DropboxClient('aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd')
# # print ('linked account: ', client.account_info())
# #Display on Screen = "Account Linked"

# #Display on Screen = "Syncing Files"

# dbx = dropbox.Dropbox('aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd')
# #Display on Screen = "Uploading Files"
# f = open("/home/meter-os/Documents/5d41402abc4b2a76b9719d911017c592_10_23_2016_18_30_14.txt", 'rb')
# #with open(file_path) as f:
# dbx.files_upload(f, dest_path, mute=True)
# print ("uploaded:", response)
# print("DropBox Updated")

# #
# # rootdir = '/tmp/test'

# print ("Attempting to upload...")
# # walk return first the current folder that it walk, then tuples of dirs and files not "subdir, dirs, files"
# for dir, dirs, files in os.walk(rootdir):
#     for file in files:
#         try:
#             file_path = os.path.join(dir, file)
#             dest_path = os.path.join('/test', file)
#             print 'Uploading %s to %s' % (file_path, dest_path)

#         except Exception as err:
#             print("Failed to upload %s\n%s" % (file, err))

# print("Finished upload.")
# TOKEN = 'aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd'
# parser.add_argument('--token', default=TOKEN,
#                     help='Access token '
# '(see https://www.dropbox.com/developers/apps)')
dbx = dropbox.Dropbox("aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd")

def download(_path):
    """Download a file.
    Return the bytes of the file, or None if it doesn't exist.
    """
    path = "//" + _path
    while '//' in path:
        path = path.replace('//', '/')

    try:
        md, res = dbx.files_download(path)
    except dropbox.exceptions.HttpError as err:
        print('*** HTTP error', err)
        return None
    data = res.content
    save_path = "C:\Users\joshu\Desktop\codejam\{0}".format(_path)

    file = open(save_path, "w")
    file.write(data)
    file.close()
    return data


def list_folder(dbx):
    """List a folder.
    Return a dict mapping unicode filenames to
    FileMetadata|FolderMetadata entries.
    """
    path = '/'
    while '//' in path:
        path = path.replace('//', '/')
    path = path.rstrip('/')
    try:

        res = dbx.files_list_folder(path)
    except dropbox.exceptions.ApiError as err:
        print('Folder listing failed for', path, '-- assumped empty:', err)
        return {}
    else:
        rv = {}
        for entry in res.entries:
            rv[entry.name] = entry
        return rv


# print upload()
hashes = []
files_do_download = list_folder(dbx).keys()
for file_do_download in files_do_download:
    _hash_name = file_do_download[:-4]
    hash_name = _hash_name.split("_", 1)[0]
    hashes.append(hash_name)
    date = _hash_name.split("_", 1)[1]
    # print hash_name, date
    # print file_do_download

    # download latest one for each hash

unique_hashes = list(set(hashes))

print unique_hashes

# find all the files with this hash
for unique_hash in unique_hashes:
    # if
    # get the latest
    pass


# print files_do_download

