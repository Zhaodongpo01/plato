import sys

def test(id, username):
    print('id:', id, 'username', username)

if __name__ == '__main__':
    test(sys.argv[1], sys.argv[2])
    return 100