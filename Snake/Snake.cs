using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Snake : MonoBehaviour
{   
    private Camera myCamera;
    private float cameraSize;
    public Vector2[,] grid;
    private Vector3 direction;
    public GameObject foodPrefab;
    public GameObject bodyPrefab;
    private List<GameObject> snake;
    private GameObject food;
    void Start()
    {
        myCamera = Camera.main;
        cameraSize = myCamera.orthographicSize;
        grid = new Vector2[(int)cameraSize * 2, (int)cameraSize * 2];
        direction = new Vector3(-1, 0, 0);

        GenerateMap();

        CreateFood();

        // init list
        snake = new List<GameObject>();

        // add the head of the snake
        GameObject head = (GameObject) Instantiate(bodyPrefab, new Vector3( grid[(int)cameraSize, (int)cameraSize].x, grid[(int)cameraSize, (int)cameraSize].y ), Quaternion.Euler(0, 0, 0));
        snake.Add(head);
    }

    void Update() {
        Vector3 headPos = snake[0].transform.position;

        EatFood();

        if (Input.GetKeyDown("left")) {
            if (direction != new Vector3(1, 0, 0)) {
                direction = new Vector3(-1, 0, 0);
            }
        }
        else if (Input.GetKeyDown("right")) {
            if (direction != new Vector3(-1, 0, 0)) {
                direction = new Vector3(1, 0, 0);
            }
        }
        else if (Input.GetKeyDown("up")) {
            if (direction != new Vector3(0, -1, 0)) {
                direction = new Vector3(0, 1, 0);
            }

        }
        else if (Input.GetKeyDown("down")) {
            if (direction != new Vector3(0, 1, 0)) {
                direction = new Vector3(0, -1, 0);
            }
        }

        // move the snake
        snake.Insert(0, (GameObject) Instantiate(bodyPrefab, headPos + direction, Quaternion.Euler(0, 0, 0)));
        Destroy(snake[snake.Count - 1]);
        snake.RemoveAt(snake.Count - 1);

        ifCollision();
    }

    private void EatFood() {
        if (Mathf.Abs((snake[0].transform.position - food.transform.position).magnitude) < 0.1 && food != null) {
            GameObject tail = snake[snake.Count - 1];
            Vector3 tailPos = new Vector3(tail.transform.position.x, tail.transform.position.y);
            Vector3 newTailPos = tailPos - new Vector3(direction.x, direction.y);
            snake.Add((GameObject) Instantiate(bodyPrefab, newTailPos, Quaternion.Euler(0,0,0)));
            Destroy(food);
            CreateFood();
        }
    }

    private void ifCollision() {
        Vector3 headPos = snake[0].transform.position;
        for (int i = 1; i < snake.Count; i++) {
            Vector3 otherPos = snake[i].transform.position;
            if (Mathf.Abs( (headPos - otherPos ).magnitude) < .1f ) {
                Debug.Log("Game over");
                Application.Quit();
            }
        }
    }


    private void CreateFood() {
            int xIndex = Random.Range(0, (int) cameraSize * 2 - 1);
            int yIndex = Random.Range(0, (int) cameraSize * 2 - 1);
            Debug.Log(xIndex);
            Debug.Log(yIndex);
            food = (GameObject) Instantiate(foodPrefab, new Vector3( grid[yIndex, xIndex].x, grid[yIndex, xIndex].y ) , Quaternion.Euler(0,0,0));
    }
    private void GenerateMap() {
        for (int y = 0; y < cameraSize * 2; y++) {
            for (int x = 0; x < cameraSize * 2; x++) {
                grid[y, x] = new Vector2(-cameraSize + x + 0.5f, cameraSize - y - 0.5f);
            }
        }
    }

    private void OnDrawGizmos() {
        if (grid != null) {
            for (int y = 0; y < grid.GetLength(0); y++) {
                for (int x = 0;  x < grid.GetLength(1); x++) {
                    Gizmos.color = Color.green;
                    Vector3 pos = new Vector3( grid[y,x].x, grid[y, x].y, 0);
                    Gizmos.DrawSphere(pos, .5f);   
                }
            }
        }
    }
}
