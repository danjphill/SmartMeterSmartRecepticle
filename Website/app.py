from flask import Flask, render_template, request, jsonify, redirect, url_for, Response
import dropbox
import datetime, time
from flask.ext.login import LoginManager , login_required , UserMixin , login_user
# website Code
app = Flask(__name__)

app.secret_key = 'xxxxyyyyyzzzzz'

login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'


class User(UserMixin):
	def __init__(self , username , password , id , active=True):
		self.id = id
		self.username = username
		self.password = password
		self.active = active

	def get_id(self):
		return self.id
	
	def is_active(self):
		return self.active

	def get_auth_token(self):
		return make_secure_token(self.username , key='secret_key')

# @app.route("/login")
# def login():

# 	return render_template('login.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['inputEmail']
        password = request.form['inputPassword']
        print username
        print password
        print "hello"        
        if (password == "admin") and (username == "admin@test.com"):
			user = User(122)
			login_user(user)
			return redirect(url_for('index'))
   	return render_template('login.html')

@app.route("/")
def index():
#Home Page Where the User List Is generate
	users = list_folder()
	users_to_show = []
	
	for user in users:
		user = user.split('_')
		if user[1] == 'Readings.txt':
			users_to_show.append(user[0])




	return render_template('users.html', users = users_to_show)

@app.route("/trends/<user>")
def trends(user):
    # plots graph data

    all_data = []

    data = download("/"+user+"_Readings.txt")

    data = data.split('\n')
    for line in data:
    	line = line.split(',')
    	if line[0] == 'CumPower':
    		power = float(line[1])
    		date_time = line[2]+"_"+line[3]
    		date_time = time.mktime(datetime.
                        datetime.
                        strptime(date_time,
                                 '%m_%d_%Y_%H_%M_%S').
                        timetuple())
        	date_time = date_time*1000

        	current_data = [date_time, power]
        	all_data.append(current_data)

    		# print date_time
    	# if line = ''

    

    # data = [[], [], []]
    return render_template('trends.html', all_data=all_data)

def list_folder():
    """List a folder.
    Return a dict mapping unicode filenames to
    FileMetadata|FolderMetadata entries.
    """
    path = ''
    dbx = dropbox.Dropbox("aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd")
    try:        
        res = dbx.files_list_folder(path)
    except dropbox.exceptions.ApiError as err:
        print('Folder listing failed for', path, '-- assumped empty:', err)
        return {}
    else:
        rv = {}
        for entry in res.entries:
            rv[entry.name] = entry
    return rv.keys()



def download(path):
    dbx = dropbox.Dropbox("aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd")  
    try:
        md, res = dbx.files_download(path)
    except dropbox.exceptions.HttpError as err:
        print('*** HTTP error', err)
        return None
    data = res.content
    return data

if __name__ == "__main__":
    app.run(debug=True, threaded=True)

@app.route("/logout")
@login_required
def logout():
    logout_user()
    return redirect(url_for('login'))

@login_manager.user_loader
def load_user(userid):
	return users_repository.get_user_by_id(userid)
