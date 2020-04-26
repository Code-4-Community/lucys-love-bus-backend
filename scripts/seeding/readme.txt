llb_route_healthcheck.py script is meant to simply seed a live instance of the service with test data by means of calling a sequence of routes as a user would do. We allow the definition of user and event data which gets populated into the database; see seed_data folder. It then prints to console all of the access token of users you have defined for to easy use in postman/further testing. 

Step one:

	define desired test data in create_event_data.py and new_user_data.py. Test data takes form of list of python dicts. Those dicts mimic the schema of our json request body for create_event and sign_up route respectively. Leave the admin user data one as the script relies on admins existance to create events using the create event route.

Step two: 

	enter your properties in props.py

run "python llb_route_healthcheck.py". Ideally you should see the status codes of the routes that were called and the access tokens of all the test users in new_user_data.py.

Note: some of this script relies on manual sql to get around some missing routes.

Need to have:

python 3

psycodb2 (pip install psycodb2)

requests (pip install requests)




