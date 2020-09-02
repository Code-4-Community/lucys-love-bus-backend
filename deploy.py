import os
import requests

# The directory that contains all `.properties.example` files
PROPERTIES_DIR = "common/src/main/resources/properties/"

# Dictionary that maps example file names to a dictionary where the keys
#   are the PROPERTIES (KEYS), and the values are ENVIRONMENT VARIABLES
ENV_VALUES = {
    "aws.properties.example": {
        "aws_access_key": "AWS_ACCESS_KEY",
        "aws_secret_key": "AWS_SECRET_KEY",
    },

    "db.properties.example": {
        "database.url": "DB_DOMAIN",
        "database.username": "DB_USERNAME",
        "database.password": "DB_PASSWORD",
    },

    "stripe.properties.example": {
        "stripe_api_secret_key": "STRIPE_API_SECRET",
        "stripe_webhook_signing_secret": "STRIPE_WEBHOOK_SECRET",
    },

    "emailer.properties.example": {
        "sendPassword": "GMAIL_APP_PASSWORD",
        "shouldSendEmails": "GMAIL_APP_ENABLED",
    },

    "jwt.properties.example": {
        "secret_key": "JWT_SECRET_KEY",
    },

    "slack.properties.example": {
        "slack_webhook_url": "SLACK_WEBHOOK_URL",
        "enabled": "SLACK_ENABLED",
    },

}

# Whether or not to log errors to Slack
SEND_SLACK = True
# The environment variable with the Slack webhook URL
SLACK_WEBHOOK_ENV_VAR = "SLACK_WEBHOOK_URL"


def main():
    global SEND_SLACK
    SEND_SLACK = True if SLACK_WEBHOOK_ENV_VAR in os.environ else False
    dir_contents = os.listdir(PROPERTIES_DIR)

    for file_name in dir_contents:
        # Only copy/modify .properties.example files
        if not file_name.endswith(".properties.example"):
            continue

        print("Reading:", file_name)

        out_file_name = file_name[:-8]
        print("Writing:", out_file_name)

        with open(PROPERTIES_DIR + out_file_name, "w") as file_properties:
            with open(PROPERTIES_DIR + file_name, "r") as file_example:
                process_properties(file_name, file_example, file_properties)


def process_properties(example_file_name, file_example, file_properties):
    """
    Given the name of an example file and IO files for reading and writing, copies
    the contents of the example file to the properties file. All placeholder
    values present in ENV_VALUES are replaced with their respective values.

    :param example_file_name: the name of the example file.
    :param file_example: the IO file to read with example properties.
    :param file_properties: the IO file to write the production properties.
    :return: void
    """
    # If no replacement values are provided, copy the .example file as-is
    if example_file_name not in ENV_VALUES:
        for line in file_example:
            file_properties.write(line)
        return

    # Key: property, value: environment variable
    replacement_dict = ENV_VALUES[example_file_name]

    # Otherwise, copy and replace the placeholders with environment variables
    for line in file_example:
        line_split = line.split("=", 1)

        # Copy the whole line if it did not split correctly
        if len(line_split) != 2:
            file_properties.write(line)
            continue

        # Copy the the whole line if no replacement var is provided
        key_placeholder = line_split[0].strip()  # The key before the "="
        if key_placeholder not in replacement_dict:
            file_properties.write(line)
            continue

        # Else, copy the line and replace the placeholder
        if key_placeholder in replacement_dict:
            env_var = replacement_dict[key_placeholder]

            # Ensure the desired environment variable is actually set (and strip leading "$")
            env_var = env_var if not env_var.startswith("$") else env_var[1:]
            if env_var not in os.environ:
                error_msg = "`[{}]`: Replacing empty environment variable: `${}`".format(example_file_name, env_var)
                handle_error(error_msg)

            out_line = "{} = ${}\n".format(key_placeholder, env_var)
            file_properties.write(out_line)
            continue


def handle_error(error_msg):
    """
    Handles printing error messages and sending them to Slack, if applicable.

    :param error_msg: the error message to log.
    :return: void
    """
    print(error_msg)

    if SEND_SLACK:
        try:
            webhook_url = os.environ.get(SLACK_WEBHOOK_ENV_VAR)
            payload = {
                "text": error_msg
            }
            requests.post(webhook_url, json=payload)
        except Exception as e:
            print("Failed to send Slack alert:", e)


main()