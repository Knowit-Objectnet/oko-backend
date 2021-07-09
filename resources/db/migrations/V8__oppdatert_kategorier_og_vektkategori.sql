ALTER TABLE kategori
ADD COLUMN vektkategori boolean  NOT NULL DEFAULT FALSE;

INSERT INTO kategori (id, navn) VALUES ('6a5d7600-e6af-4848-8c7b-c255297f23ef', 'Dyreutstyr');
INSERT INTO kategori (id, navn, vektkategori) VALUES ('5cc7e9bc-d86d-4a09-88b5-5d6e43c6bd15', 'Sport- og friluftsutstyr', true);
INSERT INTO kategori (id, navn, vektkategori) VALUES ('b5546568-92de-4422-8ba3-a77e5d13abfa', 'Tekstiler', true);
INSERT INTO kategori (id, navn, vektkategori) VALUES ('79d7467a-0f59-4581-b226-a5201ad83ee3', 'Hvitevarer', true);

UPDATE kategori
SET vektkategori=true
WHERE id in ('24a6786f-278d-4215-916a-dc467f6c828b', 'b1c6de98-4a11-4526-8298-f16886e5b552');

UPDATE kategori
SET navn='Barneutstyr og leker'
WHERE id='cc4912ef-e2ed-4460-9c50-39caffde79de';

UPDATE kategori
SET navn='Dekorasjon og interiør'
WHERE id='35a95968-444c-45a8-93f2-e413295d4af1';

UPDATE kategori
SET navn='Store møbler'
WHERE id='e19d6892-d3e7-4699-9d2d-05407dfaaf7d';

