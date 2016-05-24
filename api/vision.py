import base64
import os
import re
import sys

from googleapiclient import discovery
from googleapiclient import errors
from oauth2client.service_account import ServiceAccountCredentials

scopes = ['https://www.googleapis.com/auth/cloud-platform']
DISCOVERY_URL = 'https://{api}.googleapis.com/$discovery/rest?version={apiVersion}'  # noqa


class VisionApi:

    def __init__(self):
        self.credentials = ServiceAccountCredentials.from_json_keyfile_name('vision-key.json', scopes=scopes)
        self.service = discovery.build(
            'vision', 'v1', credentials=self.credentials,
            discoveryServiceUrl=DISCOVERY_URL)

    def detect_text(self, filename, num_retries=1, max_results=1):
        with open(filename, 'rb') as image:
            
            batch_request = []
            batch_request.append({
                'image': {
                    'content': base64.b64encode(image.read()).decode('UTF-8')
                },
                'features': [{
                    'type': 'TEXT_DETECTION',
                    'maxResults': max_results
                }]
            })

            
        request = self.service.images().annotate(
            body={'requests': batch_request})

        try:
            responses = request.execute(num_retries=num_retries)
            print responses
            if 'responses' not in responses:
                return {}
            text_response = None
            response = responses[0]
            if 'error' in response:
                print("API Error for %s: %s" % (
                        filename,
                        response['error']['message']
                        if 'message' in response['error']
                        else ''))
            if 'textAnnotations' in response:
                text_response = response['textAnnotations']['description']
            else:
                text_response = None
            print "HEY !!!"
            print text_response
            return text_response
        except errors.HttpError as e:
            print("Http Error for %s: %s" % (filename, e))
        except KeyError as e2:
            print("Key error: %s" % e2)

        return None


if __name__ == "__main__":
    v = VisionApi()
    v.detect_text("/tmp/ppp/JPEG_20160524_072352_760680689.jpg")
