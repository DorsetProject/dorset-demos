Python Remote Agent
===================
The Dorset framework supports both local agents running in the same JVM and remote agents that implement the remote agent API. This demonstrates how to create a remote agent in python. It uses the Flask web framework and pyephem for astronomical algorithms. The remote agent can answer questions like "when is the next full moon?".

Install
-----------
Install the dependencies using pip: 

```
pip install -r requirements.txt
```

Run
----------
Run the python remote agent with:

```
python server.py
```

Test
-----------
You can verify that the web service is up and running by hitting the ping endpoint with your web browser: http://localhost:5000/ping
