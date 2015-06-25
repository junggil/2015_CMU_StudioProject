# smtplib module send mail

import smtplib
import pickle
from os import path

from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

CONTENTS = {
        'confirm' : '''Thank you for signing up for IoT anyware. We're really happy to have you! Click the link below to activate account: <a href="%s" target="_blank">Email Confirmation</a>''',
        'alarm' : '''<h3 style="color:red"> Alarm! </h3> %s'''
}

def _sendMail(mailTo, subject, text, isHtml=False):
    server = smtplib.SMTP('smtp.gmail.com', 587)
    server.ehlo()
    server.starttls()
    gmail_sender, gmail_password = pickle.load(open(path.join(path.dirname(__file__), 'account.p'),'rb'))
    server.login(gmail_sender, gmail_password)

    msg = MIMEMultipart('alternative')
    msg['To'] = mailTo
    msg['Subject'] = subject 
    msg['From'] = gmail_sender
    msg.attach(MIMEText(text, 'html') if isHtml else MIMEText(text, 'plain'))
    server.sendmail(gmail_sender, [mailTo], msg.as_string())
    server.quit()

def sendConfirmMail(mailTo, name, url):
    html_template_file = path.join(path.dirname(path.dirname(__file__)), 'templates', 'email_template.html')
    text = open(html_template_file).read().replace('{{name}}', name).replace('{{contents}}', CONTENTS['confirm'] % url)
    _sendMail(mailTo, 'Invitation to IoT anyware', text, isHtml=True)

def sendAlarm(mailTo, name, contents):
    html_template_file = path.join(path.dirname(path.dirname(__file__)), 'templates', 'email_template.html')
    text = open(html_template_file).read().replace('{{name}}', name).replace('{{contents}}', CONTENTS['alarm'] % contents)
    _sendMail(mailTo, 'Alarm on IoT anyware', text, isHtml=True)

if __name__ == '__main__':
    TO = 'sooshia@gmail.com'
    SUBJECT = '[SMTP] TEST MAIL'
    TEXT = open('email_confirm.html').read().replace('%(name)', 'Justine').replace('%(url)', 'http://www.google.com')
    _sendMail(TO, SUBJECT, TEXT, isHtml=True)
