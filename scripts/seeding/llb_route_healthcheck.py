# pip install requests
from requests import * 
# pip install psycopg2
from psycopg2 import *

from props import *
from seed_data.new_user_data import *
from seed_data.create_event_data import *

import json 
 
def run_sql(file_name):

    """ Input: relative file path to sql file. 
        Runs sql against db specified in props. 
    """

    try:

        fd = open(file_name, 'r')

        sqlFile = fd.read()

        fd.close()

        commands = sqlFile.split(';')

    except IOError:
        print(IOError)
        return

    try:

        conn = connect(**db_creds)

        cur = conn.cursor()

    except (Exception) as error:
        print(error)
        return 

    for command in commands:
        try:
            if command.strip () != '':
                cur.execute(command)
        except (Exception) as error:
            print(error, "command skipped: ", command)

    conn.commit()

def signup_request(*user_data):

    """ Input: list of dicts list of dicts mimicking format of json request bodu
        Makes a signup request to specified host for each user. 
        Returns a dict relating users first name and access token.
    """

    user_access_tokens = {}

    for data in user_data:


        try:
            r = post(host + "/user/signup", data=json.dumps(data))
            print("signup :", r.status_code)
            user_access_tokens[data["firstName"]] = r.json()
        
        except Exception as e:
            print(e)

    return user_access_tokens

def create_events(admin_access_token, *create_event_data):

    """ Input: valid access token for admin user. List of dicts 
        mimicking the format of the create event json request body.
    """

    header = {'X-Access-Token' : admin_access_token}

    for data in create_event_data:

        try:
            r = post(host + "/protected/events/", data=json.dumps(data), headers=header)
            print("create events: ", r.status_code)
        
        except Exception as e:
            print(e)

def login_user(user_data):

    """ Input: a dict mimicking the format of login json request body.
        Returns access token.
    """

    payload = {
        'email' : user_data['email'],
        'password' : user_data['password']
    }

    try:
        r = post(host + '/user/login', data=json.dumps(payload))
        print('login: ', r.status_code)
        return r.json()["accessToken"]
    except Exception as e:
        print(e)


def seed_data():

    """ Seeds test data against a live instance of service. 
            1. wipes db
            2. creates users defined in new_user_data.py
            3. updates "admin" user to have admin priv with sql
            4. logs in admin and gets access token
            5. creates events defined in create_event_data.py with admin token
            6. signs up all users to all events using sql
            7. prints access tokens for users to use in postman/further testing

        Note: should replace sql in steps 3 and 6 when routes get created.
    """

    run_sql('sql/clear_db.sql') 
    tokens = signup_request(*test_users)
    run_sql('sql/admin_user.sql') 
    adminToken = login_user(test_users[2])
    create_events(adminToken, *test_events)
    run_sql('sql/user_events_assoc.sql') 
    print(json.dumps(tokens, indent=2))

if __name__ == "__main__":

    seed_data()




