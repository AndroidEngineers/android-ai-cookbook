#!/usr/bin/env python3
"""Validate recipe metadata without third-party dependencies or network access."""
import json
import re
import sys
from datetime import date
from pathlib import Path
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]
STATUSES = {'draft', 'tested-fixture', 'tested-live', 'experimental', 'needs-update', 'retired'}
TESTED = {'tested-fixture', 'tested-live'}

def require(condition, message):
    if not condition:
        raise ValueError(message)

def local_path(value, field):
    require(isinstance(value, str) and value.strip(), f'{field}: missing path')
    p = (ROOT / value).resolve()
    require(p.is_relative_to(ROOT) and p != ROOT, f'{field}: path must stay inside repository')
    require(p.exists(), f'{field}: path does not exist: {value}')
    return p

def validate():
    entries = json.loads((ROOT / 'catalog/recipes.json').read_text())
    require(isinstance(entries, list) and entries, 'catalog must be a nonempty list')
    ids = set()
    published = 0
    for r in entries:
        require(isinstance(r, dict), 'entry must be an object')
        ident = r.get('id')
        require(isinstance(ident, str) and re.fullmatch(r'[a-z0-9]+(?:-[a-z0-9]+)*', ident), 'invalid recipe ID')
        require(ident not in ids, f'duplicate ID: {ident}')
        ids.add(ident)
        for field in ['title', 'track', 'owner']:
            require(isinstance(r.get(field), str) and r[field].strip(), f'{ident}: missing {field}')
        require(r.get('status') in STATUSES, f'{ident}: invalid status')
        track = local_path('recipes/' + r['track'], ident + '.track')
        require(track.is_dir(), f'{ident}: track must be a directory')
        doc = local_path(r.get('doc_path'), ident + '.doc_path')
        require(doc.is_file() and doc.is_relative_to(track), f'{ident}: recipe documentation must be inside its track')
        url = urlparse(r.get('academy_url', ''))
        require(url.scheme == 'https' and url.netloc == 'www.androidengineers.in' and re.fullmatch(r'/roadmap/[a-z0-9-]+/lesson/[a-z0-9-]+', url.path), f'{ident}: invalid academy lesson URL')
        require(isinstance(r.get('tested_versions'), dict), f'{ident}: tested_versions must be an object')
        require(isinstance(r.get('tested_devices'), list), f'{ident}: tested_devices must be a list')
        if r.get('code_path') is not None:
            local_path(r['code_path'], ident + '.code_path')
        if r['status'] in TESTED:
            local_path(r.get('code_path'), ident + '.code_path')
            evidence = local_path(r.get('evidence_path'), ident + '.evidence_path')
            require(evidence.is_file() and evidence.read_text().strip(), f'{ident}: evidence file must not be empty')
            require(isinstance(r.get('release_tag'), str) and r['release_tag'].strip(), f'{ident}: missing release_tag')
            require(r['tested_versions'] and all(isinstance(v, str) and v.strip() for v in r['tested_versions'].values()), f'{ident}: missing tested_versions')
            verified = date.fromisoformat(r.get('last_verified') or '')
            require(verified <= date.today(), f'{ident}: verification date is in the future')
            published += 1
    print(f'Catalog OK: {len(entries)} entries; {published} tested recipes; {len(entries) - published} unverified or inactive entries.')

if __name__ == '__main__':
    try:
        validate()
    except (ValueError, TypeError, KeyError, OSError) as error:
        print(f'Catalog error: {error}', file=sys.stderr)
        sys.exit(1)
