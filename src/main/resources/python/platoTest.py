import sys

def test(company, username):
    print('company:', company, 'username', username)

if __name__ == '__main__':
    test(sys.argv[1], sys.argv[2])