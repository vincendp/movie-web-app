use moviedb;

DELIMITER $$
DROP PROCEDURE IF EXISTS add_star $$
CREATE PROCEDURE add_star (IN name VARCHAR(100), IN year INT, OUT new_id_str VARCHAR(20))
BEGIN
        DECLARE new_id INTEGER;
        SELECT CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED INTEGER) INTO new_id
    FROM stars;
        SET new_id = new_id + 1;
        SET new_id_str = CONCAT("nm", CAST(new_id AS CHAR));

    IF ISNULL(year) THEN
                INSERT INTO stars (id, name, birthYear)
                VALUES (new_id_str, name, NULL);
    ELSE
                INSERT INTO stars (id, name, birthYear)
                VALUES (new_id_str, name, year);
        END IF;
END $$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS add_genre $$
CREATE PROCEDURE add_genre (IN name VARCHAR(100), OUT new_id INTEGER)
BEGIN
        DECLARE max_temp INTEGER;
        SELECT MAX(id) INTO max_temp
    FROM genres;
    Set new_id = max_temp + 1;
    INSERT INTO genres (id, name)
		VALUES (new_id, name);
END $$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS add_movie $$
CREATE PROCEDURE add_movie (IN title VARCHAR(100), IN year INT, 
IN director VARCHAR(100), IN star_name VARCHAR(100), 
IN genre_name VARCHAR(100), OUT moviesMessage INT, OUT genresMessage INT, OUT starsMessage INT)

sp:BEGIN
		DECLARE new_id_str VARCHAR(255);
        DECLARE max_temp INTEGER;
        DECLARE e_id VARCHAR(20); -- FOR CHECKING IF MOVIE ALREADY EXISTS
        DECLARE s_id VARCHAR(20); -- ID FOR NEW STAR
        DECLARE g_id INTEGER; -- ID FOR NEW GENRE
        DECLARE starExists VARCHAR(20); -- IF MOVIE ALREADY EXISTS, CHECK TO SEE IF STAR ALREADY EXISTS
        DECLARE genreExists VARCHAR(20); -- IF MOVIE ALAREADY EXISTS, CHECK TO SEE IF GENRE ALREADY EXISTS
        
SELECT id INTO e_id
FROM movies
WHERE movies.title = title AND movies.year=year AND movies.director=director;
SELECT
    CAST(SUBSTRING(MAX(id), 4) AS UNSIGNED INTEGER)
INTO max_temp FROM
    movies WHERE id LIKE 'zzz%';
SELECT 
    MAX(id)
INTO s_id FROM
    stars
WHERE
    name = star_name;
SELECT 
    id
INTO g_id FROM
    genres
WHERE
    name = genre_name;
	IF ISNULL(max_temp) THEN
		SET max_temp = 0;
	ELSE
		SET max_temp = max_temp + 1;
	END IF;
    SET new_id_str = CONCAT("zzz", CAST(max_temp AS CHAR));
	IF ISNULL( e_id) THEN      -- CHECK IF MOVIE ALREADY EXISTS
        INSERT INTO movies(id, title, year, director) VALUES (new_id_str, title, year, director);
        SET moviesMessage = 0;
	ELSE                                   -- MOVIE EXISTS, CHECK TO ADD STAR AND GENRES
		SET moviesMessage = 1;
		IF ISNULL(s_id) THEN
			call add_star(star_name, NULL, @new_star_id);
			INSERT INTO stars_in_movies(starId, movieId) values (@new_star_id, e_id);
            SET starsMessage = 0;
		ELSE
				SELECT starId INTO starExists FROM stars_in_movies WHERE movieId = e_id AND starId=s_id;
                IF ISNULL(starExists) THEN
					INSERT INTO stars_in_movies(starId, movieId) values (s_id, e_id);
                    SET starsMessage = 2;
                ELSE
					SET starsMessage = 1;
                END IF;
		END IF;
                 
		IF ISNULL(g_id) THEN
			call add_genre(genre_name, @new_genre_id);
			INSERT INTO genres_in_movies(genreId, movieId) values (@new_genre_id, e_id);
            SET genresMessage = 0;
		ELSE
				SELECT genreId INTO genreExists FROM genres_in_movies WHERE movieId = e_id AND genreId=g_id;
                IF ISNULL(genreExists) THEN
                    INSERT INTO genres_in_movies(genreId, movieId) values (g_id, e_id);
                    SET genresMessage = 2;
				ELSE
					SET genresMessage = 1;
				END IF;
		END IF;
		LEAVE sp;  
	END IF;

    IF ISNULL(s_id) THEN
			call add_star(star_name, NULL, @new_star_id);
			INSERT INTO stars_in_movies(starId, movieId) values (@new_star_id, new_id_str);
			SET starsMessage = 0;
    ELSE
			INSERT INTO stars_in_movies(starId, movieId) values (s_id, new_id_str);
			SET starsMessage = 1;
	END IF;
                 
	IF ISNULL(g_id) THEN
			call add_genre(genre_name, @new_genre_id);
			INSERT INTO genres_in_movies(genreId, movieId) values (@new_genre_id, new_id_str);
			SET genresMessage = 0;
	ELSE
			INSERT INTO genres_in_movies(genreId, movieId) values (g_id, new_id_str);
            SET genresMessage = 1;
	END IF;
        
END $$
DELIMITER ;
