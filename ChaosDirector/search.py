import urllib.request
import json

def search(query):
    req = urllib.request.Request(f'https://api.modrinth.com/v2/search?query={query}', headers={'User-Agent': 'Mozilla/5.0'})
    resp = json.loads(urllib.request.urlopen(req).read())
    print(f"--- {query} ---")
    for hit in resp['hits'][:3]:
        print(hit['slug'], hit['title'])

search("scenarios")
search("clear lag")
search("optimization")
