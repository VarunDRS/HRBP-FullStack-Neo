import os
import json
import threading
import time
import schedule
from flask import Flask, request, jsonify
from slack_sdk import WebClient
from dotenv import load_dotenv
from pathlib import Path
from pymongo import MongoClient
from datetime import datetime, timedelta

env_path = Path('.') / '.env'
load_dotenv(dotenv_path=env_path)

app = Flask(__name__)

slack_client = WebClient(token=os.getenv("SLACK_BOT_TOKEN"))
BOT_ID = slack_client.api_call("auth.test")['user_id']

mongo_client = MongoClient("mongodb+srv://jagadeeshlatti:yujYuk7aBPcnRyoq@cluster0.sy7cn.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0")
db = mongo_client.get_database("hrbp")
attendance_collection = db.Attendance

user_selections = {}
member_cache = {}


def store_leave_request(user_id, user_name, leave_type, from_date, to_date, leave_reason):
    start_date = datetime.strptime(from_date, "%Y-%m-%d")
    end_date = datetime.strptime(to_date, "%Y-%m-%d")
    delta = (end_date - start_date).days + 1

    for i in range(delta):
        leave_data = {
            "userid": user_id,
            "username": user_name,
            "type": leave_type,
            "date": (start_date + timedelta(days=i)).strftime("%Y-%m-%d"),
            "reason": leave_reason,
        }
        attendance_collection.insert_one(leave_data)



def get_user_real_name(user_id):
    user_info = slack_client.users_info(user=user_id)
    return user_info["user"]["real_name"] if user_info.get("ok") else "Unknown"



def get_channel_members(channel_id):
    if channel_id in member_cache:
        return member_cache[channel_id]["members"]

    response = slack_client.conversations_members(channel=channel_id)
    if not response.get("ok"):
        return []

    members = format_members(response["members"])
    member_cache[channel_id] = {"members": members, "timestamp": time.time()}
    return members



def format_members(member_ids):
    formatted_members = []
    for member_id in member_ids:
        user_info = slack_client.users_info(user=member_id)
        if user_info.get("ok"):
            name = user_info["user"]["real_name"]
            formatted_members.append({"text": {"type": "plain_text", "text": name}, "value": member_id})
    return formatted_members



def get_leave_type_options():
    return [
        {"text": {"type": "plain_text", "text": "WFH"}, "value": "WFH"},
        {"text": {"type": "plain_text", "text": "Sick Leave"}, "value": "Sick Leave"},
        {"text": {"type": "plain_text", "text": "Unplanned Leave"}, "value": "Unplanned Leave"},
        {"text": {"type": "plain_text", "text": "Planned Leave"}, "value": "Planned Leave"},
        {"text": {"type": "plain_text", "text": "Travelling to Gurugram HQ"}, "value": "Travelling to Gurugram HQ"},
        {"text": {"type": "plain_text", "text": "Planned Leave (First Half)"}, "value": "Planned Leave (First Half)"},
        {"text": {"type": "plain_text", "text": "Planned Leave (Second Half)"}, "value": "Planned Leave (Second Half)"},
        {"text": {"type": "plain_text", "text": "Election"}, "value": "Election"},
        {"text": {"type": "plain_text", "text": "Bangalore or Delhi Holiday"}, "value": "Bangalore or Delhi Holiday"},
        {"text": {"type": "plain_text", "text": "Joined"}, "value": "Joined"}
    ]



def send_leave_request_modal(user_id, trigger_id, members):
    slack_client.views_open(
        trigger_id=trigger_id,
        view={
            "type": "modal",
            "callback_id": "leave_request_modal",
            "title": {"type": "plain_text", "text": "Leave Request"},
            "blocks": [
                {
                    "type": "input",
                    "block_id": "leave_type_block",
                    "element": {
                        "type": "static_select",
                        "action_id": "select_leave_type",
                        "placeholder": {"type": "plain_text", "text": "Select leave type"},
                        "options": get_leave_type_options()
                    },
                    "label": {"type": "plain_text", "text": "Leave Type"}
                },
                {
                    "type": "input",
                    "block_id": "from_date_block",
                    "element": {
                        "type": "datepicker",
                        "action_id": "select_from_date",
                        "initial_date": datetime.now().strftime("%Y-%m-%d"),
                        "placeholder": {"type": "plain_text", "text": "Select from date"}
                    },
                    "label": {"type": "plain_text", "text": "From Date"}
                },
                {
                    "type": "input",
                    "block_id": "to_date_block",
                    "element": {
                        "type": "datepicker",
                        "action_id": "select_to_date",
                        "initial_date": datetime.now().strftime("%Y-%m-%d"),
                        "placeholder": {"type": "plain_text", "text": "Select to date"}
                    },
                    "label": {"type": "plain_text", "text": "To Date"}
                },
                {
                    "type": "input",
                    "block_id": "manager_block",
                    "element": {
                        "type": "multi_static_select",
                        "action_id": "select_manager",
                        "placeholder": {"type": "plain_text", "text": "Select managers"},
                        "options": members
                    },
                    "label": {"type": "plain_text", "text": "Managers to be tagged"}
                },
                {
                    "type": "input",
                    "block_id": "reason_block",
                    "element": {
                        "type": "plain_text_input",
                        "action_id": "reason_input",
                        "placeholder": {"type": "plain_text", "text": "Enter the reason for your leave"}
                    },
                    "label": {"type": "plain_text", "text": "Reason"}
                }
            ],
            "submit": {"type": "plain_text", "text": "Submit"}
        }
    )



@app.route("/leave-request", methods=["POST"])
def leave_request():
    data = request.form
    user_id = data.get("user_id")
    channel_id = data.get("channel_id")

    user_selections[user_id] = {"channel_id": channel_id}

    threading.Thread(target=process_leave_request, args=(user_id, data.get("trigger_id"), channel_id)).start()

    return "", 200



def process_leave_request(user_id, trigger_id, channel_id):
    members = get_channel_members(channel_id)
    send_leave_request_modal(user_id, trigger_id, members)



@app.route("/slack/interactions", methods=["POST"])
def handle_interactions():
    data = request.form
    payload = json.loads(data["payload"])

    user_id = payload["user"]["id"]

    if payload["type"] == "view_submission":
        values = payload["view"]["state"]["values"]
        leave_type = values["leave_type_block"]["select_leave_type"]["selected_option"]["value"]
        from_date = values["from_date_block"]["select_from_date"]["selected_date"]
        to_date = values["to_date_block"]["select_to_date"]["selected_date"]
        manager_ids = [m["value"] for m in values["manager_block"]["select_manager"]["selected_options"]]
        leave_reason = values["reason_block"]["reason_input"]["value"]

        channel_id = user_selections[user_id]["channel_id"]
        manager_mentions = ' '.join([f"<@{manager_id}>" for manager_id in manager_ids])

        store_leave_request(user_id, get_user_real_name(user_id), leave_type, from_date, to_date, leave_reason)

        slack_client.chat_postMessage(
            channel=channel_id,
            text=f"User <@{user_id}> has submitted a {leave_type}\n\n"
                 f"*From Date:* {from_date}\n"
                 f"*To Date:* {to_date}\n"
                 f"*Managers:* {manager_mentions}\n"
                 f"*Reason:* {leave_reason}"
        )

        return jsonify({"response_action": "clear"})

    return jsonify({"response_type": "ephemeral"})



def update_member_cache():
    global member_cache
    for channel_id in member_cache.keys():
        member_cache[channel_id] = {"members": get_channel_members(channel_id), "timestamp": time.time()}



def run_scheduler():
    schedule.every().day.at("00:00").do(update_member_cache)
    while True:
        schedule.run_pending()
        time.sleep(60)

threading.Thread(target=run_scheduler, daemon=True).start()



if __name__ == "__main__":
    app.run(port=3000, debug=True)