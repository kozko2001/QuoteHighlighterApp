import os
from flask import Flask, request, redirect, url_for, jsonify
from werkzeug import secure_filename
from vision import VisionApi

UPLOAD_FOLDER = '/tmp/ppp/'
ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = 50 * 1024 * 1024

vision_api = VisionApi()

def allowed_file(filename):
    return '.' in filename and \
        filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

@app.route('/', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        file = request.files['file']
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            output_filename = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(output_filename)

            text = vision_api.detect_text(output_filename)
            print text

            if text:
                r = { "result": text }
            else:
                r = { "error": "something is wrong..." }
            
            return jsonify(r)

if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True)
