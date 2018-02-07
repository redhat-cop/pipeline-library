package org.redhatcop.util;

def rocketChatSend(String text, String emoji) {
  echo "Sending rocketchat message: {\"username\":\"Jenkins\",\"icon_emoji\":\"${emoji}\",\"text\": \"${text}\"}"

  //def body = JsonOutput.toJson([username: 'Jenkins', icon_emoji: emoji, text: text])
  // POST Message
  def response = httpRequest url: 'https://chat.consulting.redhat.com/hooks/CujHkriZ9vbdisMMX/S4wqzhvWN2u6YmcHFMvXis43jEAynhhFxRmN7dYEoX2LQWLe',
    httpMode: 'POST',
    requestBody: "{\"username\":\"Jenkins\",\"icon_emoji\":\"${emoji}\",\"text\": \"${text}\"}"
}

def notifyBuild(String buildStatus = 'STARTED') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
  def summary = "${subject} (${env.BUILD_URL})"
  def details = """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>"""

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
    status_icon = ':soon:'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#00FF00'
    status_icon = ':white_check_mark:'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
    status_icon = ':negative_squared_cross_mark:'
  }

  // Send notifications
  rocketChatSend(summary, status_icon)
  echo "Sending RocketChat Notification: ${buildStatus}"

}
